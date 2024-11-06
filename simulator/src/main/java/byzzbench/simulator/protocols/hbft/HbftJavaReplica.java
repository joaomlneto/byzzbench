package byzzbench.simulator.protocols.hbft;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.protocols.hbft.message.*;
import byzzbench.simulator.protocols.hbft.pojo.ReplicaRequestKey;
import byzzbench.simulator.protocols.hbft.pojo.ReplicaTicketPhase;
import byzzbench.simulator.protocols.hbft.pojo.ViewChangeResult;
import byzzbench.simulator.state.LogEntry;
import byzzbench.simulator.state.SerializableLogEntry;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Log
public class HbftJavaReplica<O extends Serializable, R extends Serializable> extends LeaderBasedProtocolReplica {

    @Getter
    private final int tolerance;

    @Getter
    private final long timeout;

    /**
     * The current sequence number for the replica.
     */
    private final AtomicLong seqCounter = new AtomicLong(1);

    /**
     * The log of received messages for the replica.
     */
    @Getter
    @JsonIgnore
    private final MessageLog messageLog;

    /**
     * The speculative execution history for the replica.
     */
    @Getter
    @JsonIgnore
    private final SpeculativeHistory speculativeHistory;

    /**
     * The set of timeouts for the replica?
     * TODO: This should not be here!!
     */
    @JsonIgnore
    private final SortedMap<ReplicaRequestKey, LinearBackoff> timeouts = new TreeMap<>();

    @Getter
    @Setter
    private volatile boolean disgruntled = false;

    public HbftJavaReplica(String replicaId,
                           SortedSet<String> nodeIds,
                           int tolerance,
                           long timeout,
                           MessageLog messageLog,
                           Transport transport) {
        super(replicaId, nodeIds, transport, new TotalOrderCommitLog());
        this.tolerance = tolerance;
        this.timeout = timeout;
        this.messageLog = messageLog;
        this.speculativeHistory = new SpeculativeHistory();
    }

    @Override
    public void initialize() {
        System.out.println("Initializing replica " + this.getNodeId());

        this.setView(1);
    }

    private void setView(long viewNumber) {
        String leaderId = this.computePrimaryId(viewNumber, this.getNodeIds().size());
        this.setView(viewNumber, leaderId);
    }

    public Collection<ReplicaRequestKey> activeTimers() {
        return Collections.unmodifiableCollection(this.timeouts.keySet());
    }

    public long checkTimeout(ReplicaRequestKey key) {
        LinearBackoff backoff = this.timeouts.get(key);
        if (backoff == null) {
            return 0L;
        }

        synchronized (backoff) {
            long elapsed = backoff.elapsed();

            /*
             * This method is called in a loop to check the timers on the requests
             * that are currently waiting to be fulfilled.
             *
             * Per PBFT 4.5.2, each time a timeout occurs, a VIEW-CHANGE vote will
             * be multicasted to the current view plus the number of timeouts that
             * have occurred and the replica waits a longer period of time until
             * the next vote is sent. The timer then waits for the sufficient number
             * of VIEW-CHANGE votes to be received before being allowed to expire
             * again after the next period of time and multicast the next
             * VIEW-CHANGE.
             */
            long remainingTime = backoff.getTimeout() - elapsed;
            if (remainingTime <= 0 && !backoff.isWaitingForVotes()) {
                this.disgruntled = true;

                long newViewNumber = backoff.getNewViewNumber();
                backoff.expire();

                ViewChangeMessage viewChange = messageLog.produceViewChange(
                        newViewNumber,
                        this.getNodeId(),
                        this.tolerance);
                this.sendViewChange(viewChange);

                /*
                 * Timer expires, meaning that we will need to wait at least the
                 * next period of time before the timer is allowed to expire again
                 * due to having to als include the time for the votes to be
                 * received and processed
                 */
                return backoff.getTimeout();
            }

            // Timer has not expired yet, so wait out the remaining we computed
            return remainingTime;
        }
    }

    private void resendReply(String clientId, Ticket<O, R> ticket) {
        ticket.getResult().thenAccept(result -> {
            long viewNumber = ticket.getViewNumber();
            RequestMessage request = ticket.getRequest();
            long timestamp = request.getTimestamp();
            long sequenceNumber = ticket.getSeqNumber();
            ReplyMessage reply = new ReplyMessage(
                    viewNumber,
                    timestamp,
                    sequenceNumber,
                    clientId,
                    this.getNodeId(),
                    result,
                    this.getSpeculativeHistory());
            this.sendReply(clientId, reply);
        }).exceptionally(t -> {
            throw new RuntimeException(t);
        });
    }

    private void recvRequest(RequestMessage request, boolean wasRequestBuffered) {
        String clientId = request.getClientId();
        long timestamp = request.getTimestamp();

        /*
         * At this stage, the request does not have a sequence number yet.
         * Since requests from individual clients are totally ordered by
         * timestamps, we attempt to identify the request processed based on
         * the origin client and the request's timestamp
         *
         * If the ticket is not null, then it indicates that the request has
         * already been fulfilled by this replica, so we resend the reply in
         * accordance with hBFT 4.1.
         */
        ReplicaRequestKey key = new ReplicaRequestKey(clientId, timestamp);
        Ticket<O, R> cachedTicket = messageLog.getTicketFromCache(key);
        if (cachedTicket != null) {
            this.resendReply(clientId, cachedTicket);
            return;
        }

        // Start the timer for this request per hBFT 4.4
        this.timeouts.computeIfAbsent(key, k -> new LinearBackoff(this.getViewNumber(), this.timeout));

        String primaryId = this.getPrimaryId();

        // hBFT 4.1 - If the request is received by a non-primary replica
        // send the request to the actual primary
        if (!this.getNodeId().equals(primaryId)) {
            this.sendRequest(primaryId, request);
            return;
        }

        /*
         * PBFT 4.2 states that buffered messages should be dispatched in a
         * group, and so when the buffer is flushed, then all requests are
         * fulfilled serially in an async manner because each reply to a
         * buffered request is guaranteed to dispatch the next buffered request.
         */
        if (!wasRequestBuffered) {
            if (messageLog.shouldBuffer()) {
                messageLog.buffer(request);
                return;
            }
        }

        long currentViewNumber = this.getViewNumber();
        long seqNumber = this.seqCounter.getAndIncrement();

        /*
         * The message log is not flat-mapped, meaning that messages are
         * organized based on the request being fulfilled rather than simply
         * being appeneded to improve the performance of processing individual
         * messages.
         *
         * The ticketing system is ordered based on the sequence number as all
         * subsequent messages between the replicas reference the sequence
         * number that this (the primary) has determined.
         */
        Ticket<O, R> ticket = messageLog.newTicket(currentViewNumber, seqNumber);
        // PBFT 4.2 - Append REQUEST
        ticket.append(request);

        PrepareMessage prepare = new PrepareMessage(
                currentViewNumber,
                seqNumber,
                this.digest(request),
                request);
        this.broadcastMessage(prepare);

        Serializable operation = request.getOperation();
        Serializable result = this.compute(new SerializableLogEntry(operation));

        ReplyMessage reply = new ReplyMessage(
                currentViewNumber,
                timestamp,
                seqNumber,
                clientId,
                this.getNodeId(),
                result,
                this.getSpeculativeHistory());

        this.sendReply(clientId, reply);

        // hBFT 4.2 - Append PREPARE
        ticket.append(prepare);

        // Move to COMMIT phase
        ReplicaTicketPhase phase = ticket.getPhase();
        ticket.casPhase(phase, ReplicaTicketPhase.COMMIT);
    }

    public void recvRequest(RequestMessage request) {
        // PBFT 4.4 - Do not accept REQUEST when disgruntled
        if (this.disgruntled) {
            return;
        }

        // hBFT 4.2 - Attempt to process non-bufferred request
        this.recvRequest(request, false);
    }

    public void sendRequest(String replicaId, RequestMessage request) {
        this.sendMessage(request, replicaId);
    }

    private boolean verifyPhaseMessage(IPhaseMessage message) {
        /*
         * The 3 phases specified by PBFT 4.2 have a common verification
         * procedure which is extracted to this method. If this procedure fails,
         * then the replica automatically halts processing because the message
         * has been sent by a faulty replica.
         */

        // PBFT 4.4 - Phase messages are not accepted when the replica is
        // disgruntled
        if (this.disgruntled) {
            return false;
        }

        long currentViewNumber = this.getViewNumber();
        long viewNumber = message.getViewNumber();
        if (currentViewNumber != viewNumber) {
            return false;
        }

        long seqNumber = message.getSequenceNumber();
        return messageLog.isBetweenWaterMarks(seqNumber);
    }

    private Ticket<O, R> recvPhaseMessage(IPhaseMessage message) {
        /*
         * The PREPARE and COMMIT messages have a common validation procedure
         * per PBFT 4.2, and has been extracted to this method.
         *
         * Both must pass the #verifyPhaseMessage(...) test as with PRE-PREPARE.
         * If the ticket for these messages have not been added yet, then a new
         * ticket will be created in order for these messages to be added to the
         * log and organized for the sequence number (see
         * #recvRequest(ReplicaRequest, boolean)) because out-of-order phase
         * messages may arrive before the corresponding PRE-PREPARE has been
         * received to set up the ticket.
         */
        if (!this.verifyPhaseMessage(message)) {
            // Return null to indicate reception handlers the message is invalid
            return null;
        }

        long currentViewNumber = message.getViewNumber();
        long seqNumber = message.getSequenceNumber();

        Ticket<O, R> ticket = messageLog.getTicket(currentViewNumber, seqNumber);
        if (ticket == null) {
            ticket = messageLog.newTicket(currentViewNumber, seqNumber);
        }

        // PBFT 4.2 - PREPARE and COMMIT messages are appended to the log
        // provided that they pass validation
        ticket.append(message);
        return ticket;
    }

    public void recvPrepare(PrepareMessage prepare) {
        if (!this.verifyPhaseMessage(prepare)) {
            return;
        }

        long currentViewNumber = this.getViewNumber();
        byte[] digest = prepare.getDigest();
        RequestMessage request = prepare.getRequest();
        long seqNumber = prepare.getSequenceNumber();

        // hBFT 4.1 - Verify request digest
        byte[] computedDigest = this.digest(request);
        if (!Arrays.equals(digest, computedDigest)) {
            return;
        }

        /*
         * PBFT 4.2 specifies that given a valid PREPARE (matching
         * signature, view and valid sequence number), the replica must only
         * accept a PREPARE given that no other PREPARE has been
         * received OR that the new PRE-PREPARE matches the digest of the one
         * that was already received.
         *
         * Upon accepting the PRE-PREPARE, the replica adds it to the log and
         * multicasts a PREPARE to all other replicas and adding the PREPARE to
         * its log.
         */
        Ticket<O, R> ticket = messageLog.getTicket(currentViewNumber, seqNumber);
        if (ticket != null) {
            // PREPARE has previously been inserted into the log for this
            // sequence number - verify the digests match per PBFT 4.1
            for (Object message : ticket.getMessages()) {
                if (!(message instanceof PrepareMessage prevPrepare)) {
                    continue;
                }

                byte[] prevDigest = prevPrepare.getDigest();
                if (!Arrays.equals(prevDigest, digest)) {
                    return;
                }
            }
        } else {
            // PREPARE is the first - create a new ticket for it in this
            // replica (see #recvRequest(ReplicaRequest, boolean) for why the
            // message log is structured this way)
            ticket = messageLog.newTicket(currentViewNumber, seqNumber);
        }

        // hBFT 4.1 - Add PREPARE along with its REQUEST to the log
        ticket.append(prepare);

        Serializable operation = request.getOperation();
        Serializable result = this.compute(new SerializableLogEntry(operation));
    
        String clientId = request.getClientId();
        long timestamp = request.getTimestamp();

        ReplyMessage reply = new ReplyMessage(
                currentViewNumber,
                timestamp,
                seqNumber,
                clientId,
                this.getNodeId(),
                result,
                this.getSpeculativeHistory());

        this.sendReply(clientId, reply);

        // hBFT 4.1 - Multicast COMMIT to other replicas
        CommitMessage commit = new CommitMessage(
                currentViewNumber,
                seqNumber,
                digest,
                request,
                this.getNodeId(),
                this.getSpeculativeHistory());
        this.broadcastMessage(commit);

        // hBFT 4.1 - Add COMMIT to the log
        ticket.append(commit);

        // Move to COMMIT phase
        ReplicaTicketPhase phase = ticket.getPhase();
        ticket.casPhase(phase, ReplicaTicketPhase.COMMIT);
        
        /*
         * Per hBFT 4.1, this replica stasfies the prepared predicate IF it has
         * valid REQUEST and PREPARE messages. 
         */
        this.tryAdvanceState(ticket, commit);
    }

    public void recvCommit(CommitMessage commit) {
        Ticket<O, R> ticket = this.recvPhaseMessage(commit);

        if (ticket != null) {
            // PBFT 4.2 - Attempt to advance the state upon reception of COMMIT
            // to perform the computation
            this.tryAdvanceState(ticket, commit);
        }
    }

    private void tryAdvanceState(Ticket<O, R> ticket, IPhaseMessage message) {
        long currentViewNumber = message.getViewNumber();
        long seqNumber = message.getSequenceNumber();
        byte[] digest = message.getDigest();

        ReplicaTicketPhase phase = ticket.getPhase();

        if (phase == ReplicaTicketPhase.PREPARE) {
            /*
             * Per hBFT 4.1 if the replica is in PREPARE phase, waiting for a PREPARE message,
             * then if it has f + 1 COMMIT messages in the log it can also COMMIT and REPLY
             */
            if (ticket.isCommittedPrepare(this.tolerance) && ticket.casPhase(phase, ReplicaTicketPhase.COMMIT)) {
                RequestMessage request = ticket.getRequest();
                if (request != null) {
                    Serializable operation = request.getOperation();
                    Serializable result = this.compute(new SerializableLogEntry(operation));
                
                    String clientId = request.getClientId();
                    long timestamp = request.getTimestamp();

                    ReplyMessage reply = new ReplyMessage(
                            currentViewNumber,
                            timestamp,
                            seqNumber,
                            clientId,
                            this.getNodeId(),
                            result,
                            this.getSpeculativeHistory());

                    this.sendReply(clientId, reply);

                    // After sending a reply to the client, send a COMMIT to other replicas
                    CommitMessage commit = new CommitMessage(
                        currentViewNumber,
                        seqNumber,
                        digest,
                        request,
                        this.getNodeId(),
                        this.getSpeculativeHistory());
                    
                    // Send to self as well in order to trigger the check for 2f + 1 COMMIT messages
                    this.broadcastMessageIncludingSelf(commit);

                    // hBFT 4.1 - Add PREPARE along with its REQUEST to the log
                    ticket.append(commit);
                }
            }
        }
    
        if (phase == ReplicaTicketPhase.COMMIT) {
            /*
             * Per hBFT 4.1 if the replica is in COMMIT phase
             * if it gets 2f + 1 COMMIT messages then completes the request
             * and adds it to its speculative execution history
             * if it gets f + 1 conflicting COMMIT messages with its PREPARE
             * then it sends a view change message
             */
            if (ticket.isCommittedLocal(this.tolerance) && ticket.casPhase(phase, ReplicaTicketPhase.COMMIT)) {
                RequestMessage request = ticket.getRequest();
                if (request != null) {
                    Serializable operation = request.getOperation();
                    Serializable result = ticket.getReply().getResult();
                
                    String clientId = request.getClientId();
                    long timestamp = request.getTimestamp();

                    // Add the execution to the speculative execution history
                    this.speculativeHistory.addEntry(seqNumber, clientId, operation, result);

                    ReplicaRequestKey key = new ReplicaRequestKey(clientId, timestamp);
                    messageLog.completeTicket(key, currentViewNumber, seqNumber);

                    this.timeouts.remove(key);
                }
            } else if (ticket.isCommittedConflicting(this.tolerance) && ticket.casPhase(phase, ReplicaTicketPhase.COMMIT)) {
                // TODO: Implement VIEW-CHANGE message
            }
        }
    }

    private void handleNextBufferedRequest() {
        RequestMessage bufferedRequest = messageLog.popBuffer();

        if (bufferedRequest != null) {
            // Process the bufferred request
            this.recvRequest(bufferedRequest, true);
        }
    }

    public void sendReply(String clientId, ReplyMessage reply) {
        //this.sendMessage(reply, clientId);
        this.sendReplyToClient(clientId, reply);

        // When prior requests are fulfilled, attempt to process the buffer
        // to ensure they are dispatched in a timely manner
        this.handleNextBufferedRequest();
    }

    public void recvCheckpoint(CheckpointMessage checkpoint) {
        /*
         * Per hBFT 4.2, there is a 3 phase checkpoint subprotocol,
         * where the protocol starts with the primary sending Checkpoint-I message, 
         * then upon receiving this the replicas send a Checkpoint-II message, 
         * upon receiving 2f+1 Checkpoint-II messages,
         * replicas send Checkpoint-III messages, and upon receiving 2f+1
         * perform GC if the checkpoint is stable.
         */
        String primaryId = this.getPrimaryId();
        
        if (checkpoint instanceof CheckpointIMessage) {
            // Only primary can send checkpoint-I, else send View-Change
            if (primaryId.equals(checkpoint.getReplicaId()) && Arrays.equals(checkpoint.getDigest(), this.digest(speculativeHistory))) {
                messageLog.appendCheckpoint(checkpoint, this.tolerance, this.speculativeHistory);
            } else {
                ViewChangeMessage viewChangeMessage = messageLog.produceViewChange(this.getViewNumber() + 1, this.getNodeId(), tolerance, speculativeHistory);
                this.broadcastMessage(viewChangeMessage);
            }
        } else if (checkpoint instanceof CheckpointIIMessage) {
            // Digest and speculative execution history should match
            if (Arrays.equals(checkpoint.getDigest(), this.digest(speculativeHistory))) {
                messageLog.appendCheckpoint(checkpoint, this.tolerance, this.speculativeHistory);
                
                // Check whether 2f + 1 Checkpoint-II messages have been seen
                // If yes then send Checkpoint-III
                boolean isCER1 = messageLog.isCER1(checkpoint, tolerance, this.speculativeHistory);
                if (isCER1) {
                    CheckpointMessage checkpointIII = new CheckpointIIIMessage(
                        checkpoint.getLastSeqNumber(),
                        this.digest(this.speculativeHistory),
                        this.getNodeId()
                    );

                    // I add it to the log as the sendCheckpoint doesnt include self
                    messageLog.appendCheckpoint(checkpointIII, this.tolerance, this.speculativeHistory);
                    this.sendCheckpoint(checkpointIII);
                }
            } else {
                ViewChangeMessage viewChangeMessage = messageLog.produceViewChange(this.getViewNumber() + 1, this.getNodeId(), tolerance, speculativeHistory);
                this.broadcastMessage(viewChangeMessage);
            }
        } else if (checkpoint instanceof CheckpointIIIMessage) {
            // Digest and speculative execution history should match
            if (Arrays.equals(checkpoint.getDigest(), this.digest(speculativeHistory))) {
                // GC will execute once 2f + 1 is received
                messageLog.appendCheckpoint(checkpoint, this.tolerance, this.speculativeHistory);
            } else {
                ViewChangeMessage viewChangeMessage = messageLog.produceViewChange(this.getViewNumber() + 1, this.getNodeId(), tolerance, speculativeHistory);
                this.broadcastMessage(viewChangeMessage);
            }
        }
    }

    public void sendCheckpoint(CheckpointMessage checkpoint) {
        // hBFT 4.2 - Multicast checkpoint
        this.broadcastMessage(checkpoint);
    }

    private void enterNewView(long newViewNumber) {
        /*
         * Enter new view by resetting the disgruntled state, updating the
         * view number and clearing any prior timeouts that the replicas have
         * been waiting on
         */
        this.disgruntled = false;
        this.setView(newViewNumber);
        this.timeouts.clear();
    }

    public void recvViewChange(ViewChangeMessage viewChange) {
        long curViewNumber = this.getViewNumber();
        long newViewNumber = viewChange.getNewViewNumber();
        String newPrimaryId = this.computePrimaryId(newViewNumber, this.getNodeIds().size());
    
        // TODO:
        // hBFT-Specific: Determine if hPANIC triggered this view change
        // if (viewChange.isTriggeredByPanic()) {
        //     this.messageLog.append(viewChange); 
        // }
    
        // Checkpoint Synchronization: Make sure speculative history matches
        if (!localDigest.equals(viewChange.getCheckpointDigest())) {
            this.sendViewChange(new ViewChangeMessage(newViewNumber, curViewNumber, localDigest, this.getNodeId()));
            return;
        }
    
        // hBFT: Process view change acceptance
        ViewChangeResult result = messageLog.acceptViewChange(viewChange, this.getNodeId(), curViewNumber, this.tolerance);
        if (result.isShouldBandwagon()) {
            ViewChangeMessage bandwagonViewChange = messageLog.produceViewChange(
                    result.getBandwagonViewNumber(), this.getNodeId(), this.tolerance, this.speculativeHistory);
            this.sendViewChange(bandwagonViewChange);
        }
    
        // Start timer to vote for next view, if applicable
        if (result.isBeginNextVote()) {
            for (LinearBackoff backoff : this.timeouts.values()) {
                synchronized (backoff) {
                    if (newViewNumber + 1 == backoff.getNewViewNumber()) {
                        backoff.beginNextTimer();
                    }
                }
            }
        }
    
        // If this replica is the new primary, multicast NEW-VIEW and update state
        if (newPrimaryId.equals(this.getNodeId())) {
            NewViewMessage newView = messageLog.produceNewView(newViewNumber, this.getNodeId(), this.tolerance);
    
            // If new-view certificate (CER2) is valid, broadcast the NEW-VIEW message
            if (newView != null) {
                this.broadcastMessage(newView);
                this.enterNewView(newViewNumber);
                
                // as of hBFT 4.3 checkpoint protocol is called after a new-view message
                CheckpointMessage checkpoint = new CheckpointIMessage(seqCounter.get(), this.digest(this.getSpeculativeHistory()), this.getNodeId());
                this.messageLog.appendCheckpoint(checkpoint, tolerance, speculativeHistory);
                this.broadcastMessage(checkpoint);
            }
        }
    }

    public void sendViewChange(ViewChangeMessage viewChange) {
        // hBFT 4.3 - Multicast VIEW-CHANGE vote
        this.broadcastMessage(viewChange);
    }

    public void recvNewView(NewViewMessage newView) {
        /*
         * Per PBFT 4.4, upon reception of a NEW-VIEW message, a replica
         * verifies that the VIEW-CHANGE votes all match the new view number.
         * If this is not the case, then the message is disregarded as it is
         * from a faulty replica.
         *
         * The set of PRE-PREPARE messages is then verified using their digests
         * and then dispatches the pre-prepares to itself by multicasting a
         * PREPARE message for each PRE-PREPARE.
         */
        if (!messageLog.acceptNewView(newView)) {
            return;
        }

        long newViewNumber = newView.getNewViewNumber();
        Collection<PrePrepareMessage> preparedProofs = newView.getPreparedProofs();
        for (PrePrepareMessage prePrepare : preparedProofs) {
            RequestMessage request = prePrepare.getRequest();
            Serializable operation = request.getOperation();

            // PBFT 4.4 - No-op request used to fulfill the NEW-VIEW constraints
            if (operation == null) {
                continue;
            }

            // PBFT 4.4 - Verify digests of each PRE-PREPARE
            byte[] digest = prePrepare.getDigest();
            if (!Arrays.equals(digest, this.digest(request))) {
                continue;
            }

            // PBFT 4.2 - Append the PRE-PREPARE to the log
            long seqNumber = prePrepare.getSequenceNumber();
            Ticket<O, R> ticket = messageLog.newTicket(newViewNumber, seqNumber);
            ticket.append(prePrepare);

            PrepareMessage prepare = new PrepareMessage(
                    newViewNumber,
                    seqNumber,
                    digest,
                    this.getNodeId());
            this.broadcastMessage(prepare);

            // PBFT 4.4 - Append the PREPARE message to the log
            ticket.append(prepare);
        }

        this.enterNewView(newViewNumber);
    }

    private String computePrimaryId(long viewNumber, int numReplicas) {
        List<String> knownReplicas = this.getNodeIds().stream().sorted().toList();
        return knownReplicas.get((int) viewNumber % numReplicas);
    }

    private String getPrimaryId() {
        return this.computePrimaryId(this.getViewNumber(), this.getNodeIds().size());
    }

    public Serializable compute(LogEntry operation) {
        this.commitOperation(operation);
        return operation;
    }

    @Override
    public void handleClientRequest(String clientId, Serializable request) throws Exception {
        // FIXME: should not get timestamp from system time
        RequestMessage m = new RequestMessage(request, System.currentTimeMillis(), clientId);
        this.recvRequest(m);
    }

    @Override
    public void handleMessage(String sender, MessagePayload m) {
        if (m instanceof RequestMessage request) {
            recvRequest(request);
            return;
        } else if (m instanceof PrePrepareMessage prePrepare) {
            recvPrePrepare(prePrepare);
            return;
        } else if (m instanceof PrepareMessage prepare) {
            recvPrepare(prepare);
            return;
        } else if (m instanceof CommitMessage commit) {
            recvCommit(commit);
            return;
        }
        throw new RuntimeException("Unknown message type");
    }

    private boolean verifyPhase(long messageViewNumber, long messageSequenceNumber) {
        // TODO: WaterMarks: check if sequence number is within the window
        return messageViewNumber == this.getViewNumber();
    }

}
