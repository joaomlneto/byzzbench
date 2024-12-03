package byzzbench.simulator.protocols.hbft;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.protocols.hbft.message.*;
import byzzbench.simulator.protocols.hbft.pojo.ReplicaRequestKey;
import byzzbench.simulator.protocols.hbft.pojo.ReplicaTicketPhase;
import byzzbench.simulator.protocols.hbft.pojo.ViewChangeResult;
import byzzbench.simulator.state.LogEntry;
import byzzbench.simulator.state.SerializableLogEntry;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.DefaultClientRequestPayload;
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

    @Getter
    private final long PANIC_TIMEOUT = 3;
    @Getter
    private final long CHECKPOINT_TIMEOUT = 5;
    @Getter
    private final long VIEWCHANGE_TIMEOUT = 5;
    @Getter
    private final long REQUEST_TIMEOUT = 10;

    /**
     * The current sequence number for the replica.
     */
    private final AtomicLong seqCounter = new AtomicLong(0);

    /* 
     * Needed for the view-change timeout
     */
    private long largestViewNumber = 0;

    /**
     * The log of received messages for the replica.
     */
    @Getter
    @JsonIgnore
    private final MessageLog messageLog;

    /**
     * The speculative execution history for the replica.
     * Sometimes called the commit certificate.
     * It includes requests that are considered committed,
     * in other words the replica got 2f + 1 COMMIT messages
     * for the request.
     */
    @Getter
    @JsonIgnore
    private SpeculativeHistory speculativeHistory;

    /**
     * The speculatively executed requests.
     * This is not the same as speuclative history.
     * This includes every request that had been accepted,
     * by the replica, either through prepare or f + 1 COMMITs.
     */
    @Getter
    @JsonIgnore
    private final SortedMap<Long, RequestMessage> speculativeRequests;

    @Getter
    @Setter
    private volatile boolean disgruntled = false;

    @Getter
    @Setter
    private volatile boolean checkpointForNewView = false;

    // The received requests stored as <timestamp, clientId>
    private final SortedMap<Long, String> receivedRequests = new TreeMap<>();

    public HbftJavaReplica(String replicaId,
                           SortedSet<String> nodeIds,
                           int tolerance,
                           long timeout,
                           MessageLog messageLog,
                           Scenario scenario) {
        super(replicaId, scenario, new TotalOrderCommitLog());
        this.tolerance = tolerance;
        this.timeout = timeout;
        this.messageLog = messageLog;
        this.speculativeHistory = new SpeculativeHistory();
        this.speculativeRequests = new TreeMap<>();
    }

    @Override
    public void initialize() {
        System.out.println("Initializing replica " + this.getId());

        this.setView(1);
    }

    public void setView(long viewNumber) {
        String leaderId = this.computePrimaryId(viewNumber, this.getNodeIds().size());
        this.setView(viewNumber, leaderId);
    }

    private void clearSpecificTimeout(String description) {
        this.clearTimeout(description);
    }

    private void resendReply(String clientId, Ticket<O, R> ticket) {
        ticket.getResult().thenAccept(result -> {
            long viewNumber = ticket.getViewNumber();
            RequestMessage request = ticket.getRequest();
            long timestamp = request.getTimestamp();
            long sequenceNumber = ticket.getSeqNumber();

            // Also possbile option
            // ReplyMessage prevReply = ticket.getReply();

            ReplyMessage reply = new ReplyMessage(
                    viewNumber,
                    timestamp,
                    sequenceNumber,
                    clientId,
                    this.getId(),
                    result,
                    this.speculativeHistory);
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

        if (this.receivedRequests.get(request.getTimestamp()) != null) {
            return;
        }
        this.receivedRequests.put(request.getTimestamp(), request.getClientId());


        String primaryId = this.getPrimaryId();

        // Start the timer for this request per hBFT 4.3
        // This timeout check whether a request is completed in a given time
        this.setTimeout(this::sendViewChangeOnTimeout, this.REQUEST_TIMEOUT, "REQUEST" + timestamp);

        // hBFT 4.1 - If the request is received by a non-primary replica
        // send the request to the actual primary
        if (!this.getId().equals(primaryId)) {
            this.sendRequest(primaryId, request);
            return;
        }

        /*
         * PBFT 4.2 states that buffered messages should be dispatched in a
         * group, and so when the buffer is flushed, then all requests are
         * fulfilled serially in an async manner because each reply to a
         * buffered request is guaranteed to dispatch the next buffered request.
         */
        /* if (!wasRequestBuffered) {
            if (messageLog.shouldBuffer()) {
                messageLog.buffer(request);
                return;
            }
        } */

        long currentViewNumber = this.getViewNumber();
        long seqNumber = this.seqCounter.incrementAndGet();

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
        ticket.append(request);

        PrepareMessage prepare = new PrepareMessage(
            currentViewNumber,
            seqNumber,
            this.digest(request),
            request);

        this.broadcastMessage(prepare);

        Serializable operation = request.getOperation();
        Serializable result = new SerializableLogEntry(operation);

        ReplyMessage reply = new ReplyMessage(
            currentViewNumber,
            timestamp,
            seqNumber,
            clientId,
            this.getId(),
            result,
            this.getSpeculativeHistory());
        
        this.sendReply(clientId, reply);
        this.speculativeRequests.put(seqNumber, request);

        // hBFT 4.1 - Multicast COMMIT to other replicas
        CommitMessage commit = new CommitMessage(
            currentViewNumber,
            seqNumber,
            this.digest(request),
            request,
            this.getId(),
            this.speculativeHistory);

        this.broadcastMessage(commit);

        ticket.append(prepare);
        ticket.append(reply);
        ticket.append(commit);

        // Move to COMMIT phase
        ReplicaTicketPhase phase = ticket.getPhase();
        ticket.casPhase(phase, ReplicaTicketPhase.COMMIT);
    }

    public void recvRequest(RequestMessage request) {
        // hBFT 4.4 - Do not accept REQUEST when disgruntled
        // or waiting for checkpoint
        if (this.disgruntled || this.checkpointForNewView) {
            return;
        }

        // TODO: Need to check whether its from client or replica
        // hBFT 4.2 - Attempt to process non-bufferred request
        this.recvRequest(request, false);
    }

    public void sendRequest(String replicaId, RequestMessage request) {
        this.sendMessage(request, replicaId);
    }

    public boolean executedRequest(byte[] digest) {
        for (RequestMessage request : this.speculativeRequests.values()) {
            if (Arrays.equals(this.digest(request), digest)) {
                return true;
            }
        }

        return false;
    }

    public void recvPanic(PanicMessage panic) {
        // If this replica didnt exectue the request,
        // it cannot accept the PANIC message
        if (!this.executedRequest(panic.getDigest())) {
            return;
        }

        String signedBy = panic.getSignedBy();
        // Technically should never happen
        if (signedBy == null) {
            return;
        }

        this.forwardPanic(panic);
        this.disgruntled = true;

        this.messageLog.appendPanic(panic, signedBy);
        if (signedBy.equals(panic.getClientId()) || this.messageLog.checkPanics(this.tolerance) && this.getId().equals(this.getPrimaryId())) {
            CheckpointMessage checkpoint = new CheckpointIMessage(
                        this.seqCounter.get(),
                        this.digest(this.speculativeHistory),
                        this.getId(),
                        this.speculativeHistory);
            //this.sendCheckpoint(checkpoint);
            this.broadcastMessageIncludingSelf(checkpoint);
        } else if (!this.getId().equals(this.getPrimaryId()) && this.messageLog.checkPanicsForTimeout(this.tolerance)) {
            /* 
             * Start the timer for this request per hBFT 4.3
             * This timeout checks whether a checkpoint is received
             * within a time after receiving f + 1 PANICs
             */
            this.setTimeout(this::sendViewChangeOnTimeout, this.PANIC_TIMEOUT, "PANIC");
        }
    }

    public void forwardPanic(PanicMessage panic) {
        this.broadcastMessage(panic);
    }

    private boolean verifyPhaseMessage(IPhaseMessage message) {
        // Phase messages are not accepted when the replica is
        // disgruntled or waiting for checkpoint
        if (this.disgruntled || this.checkpointForNewView) {
            return false;
        }

        long currentViewNumber = this.getViewNumber();
        long viewNumber = message.getViewNumber();
        if (currentViewNumber != viewNumber) {
            return false;
        }

        long seqNumber = message.getSequenceNumber();


        /* 
         * This is different from PBFT where PBFT accepts
         * every sequence number that is between the lowWaterMark
         * and the highWaterMark. In hBFT as of 4.1 a prepare message
         * is only accepted if the seq number of the message equals
         * the local seq number + 1. Otherwise, if its a commit message
         * its accepted if the seq number equals the local seq number or 
         * the local seq number + 1. The two latter cases will be further
         * evaluated in the tryAdvanceState function.
         * 
         * However, COMMIT messages should be accepted even if they arrive later
         * as it doesn't make sense to reject them.
         */
        if (message instanceof PrepareMessage) {
            return seqNumber == this.seqCounter.get() + 1;
        } else if (message instanceof CommitMessage) {
            return this.messageLog.isBetweenWaterMarks(seqNumber);
        }

        /* 
         * This part should never trigger as the only
         * two possible messages here are PREPARE and COMMIT 
         */
        return false;
    }

    private Ticket<O, R> recvPhaseMessage(IPhaseMessage message) {
        /*
         * The PREPARE and COMMIT messages have a common validation procedure
         * per hBFT 4.1, and has been extracted to this method.
         *
         * Both must pass the #verifyPhaseMessage(...).
         * If the ticket for these messages have not been added yet, then a new
         * ticket will be created in order for these messages to be added to the
         * log and organized for the sequence number (see
         * #recvRequest(ReplicaRequest, boolean)) because out-of-order phase
         * messages may arrive before the corresponding PREPARE has been
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

        // hBFT 4.1 - PREPARE and COMMIT messages are appended to the log
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

        // Check whether the replica already executed the request
        if (this.replied(request.getClientId(), request.getTimestamp(), currentViewNumber, seqNumber)) {
            return;
        }

        /*
         * hBFT 4.1 specifies that given a valid PREPARE (matching
         * signature, view and valid sequence number), the replica must only
         * accept a PREPARE given that no other PREPARE has been
         * received OR that the new PREPARE matches the digest of the one
         * that was already received.
         */
        Ticket<O, R> ticket = messageLog.getTicket(currentViewNumber, seqNumber);
        if (ticket != null) {
            // PREPARE has previously been inserted into the log for this
            // sequence number - verify the digests match per hBFT 4.1
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
        Serializable result = new SerializableLogEntry(operation);
    
        String clientId = request.getClientId();
        long timestamp = request.getTimestamp();

        // we have the set the local seqNumber to the prepare's sequence number
        this.seqCounter.set(seqNumber);

        ReplyMessage reply = new ReplyMessage(
            currentViewNumber,
            timestamp,
            seqNumber,
            clientId,
            this.getId(),
            result,
            this.speculativeHistory);

        this.speculativeRequests.put(seqNumber, request);
        this.sendReply(clientId, reply);

        // hBFT 4.1 - Multicast COMMIT to other replicas
        CommitMessage commit = new CommitMessage(
            currentViewNumber,
            seqNumber,
            digest,
            request,
            this.getId(),
            this.speculativeHistory);
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
            if (ticket.isPrepared(this.tolerance) && ticket.casPhase(phase, ReplicaTicketPhase.COMMIT)) {
                System.out.println("Replica " + this.getId() + " received f + 1 COMMIT in PREPARE phase so should also COMMIT and REPLY");
                RequestMessage request = ticket.getRequest();

                // Checks whether the replica has executed the request
                // although, we should not be herer if executed its good to check
                if (request != null && !this.replied(request.getClientId(), request.getTimestamp(), currentViewNumber, seqNumber)) {
                    Serializable operation = request.getOperation();
                    Serializable result = new SerializableLogEntry(operation);
                
                    String clientId = request.getClientId();
                    long timestamp = request.getTimestamp();

                    if (this.seqCounter.get() + 1 == seqNumber) {
                        this.seqCounter.set(seqNumber);
                    }

                    ReplyMessage reply = new ReplyMessage(
                        currentViewNumber,
                        timestamp,
                        seqNumber,
                        clientId,
                        this.getId(),
                        result,
                        this.speculativeHistory);

                    System.out.println("Replica " + this.getId() + " REPLIED");

                    this.speculativeRequests.put(seqNumber, request);
                    this.sendReply(clientId, reply);

                    // After sending a reply to the client, send a COMMIT to other replicas
                    CommitMessage commit = new CommitMessage(
                        currentViewNumber,
                        seqNumber,
                        digest,
                        request,
                        this.getId(),
                        this.speculativeHistory);
                    
                    // Send to self as well in order to trigger the check for 2f + 1 COMMIT messages
                    this.broadcastMessage(commit);

                    // hBFT 4.1 - Add COMMIT to the log
                    ticket.append(commit);
                    this.tryAdvanceState(ticket, commit);
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
            if (ticket.isCommittedLocal(this.tolerance)) {
                RequestMessage request = ticket.getRequest();
                if (request != null && !request.equals(this.speculativeHistory.getHistory().get(seqNumber))) {
                    String clientId = request.getClientId();
                    long timestamp = request.getTimestamp();

                    // Add operation to commitLog
                    Serializable operation = request.getOperation();
                    this.compute(seqNumber, new SerializableLogEntry(operation));

                    // Add the execution to the speculative execution history
                    this.speculativeHistory.addEntry(seqNumber, request);

                    ReplicaRequestKey key = new ReplicaRequestKey(clientId, timestamp);
                    messageLog.completeTicket(key, currentViewNumber, seqNumber);

                    // Clear the timeout for request completion
                    this.clearSpecificTimeout("REQUEST" + request.getTimestamp());
                }

                /*
                 * Checkpointing is specified by hBFT 4.2. A checkpoint is
                 * reached every time a number of requests have been executed
                 * by the primary or receives 2f + 1 panic messages.
                 * The number of executed requests is not defined, so I will
                 * use the same as for PBFT, where the sequence number mod 
                 * the interval reaches 0.
                 */
                if (seqNumber % this.messageLog.getCheckpointInterval() == 0 && this.getId().equals(this.getPrimaryId())) {
                    CheckpointMessage checkpoint = new CheckpointIMessage(
                        seqNumber,
                        this.digest(this.speculativeHistory),
                        this.getId(),
                        this.speculativeHistory);
                    //this.sendCheckpoint(checkpoint);
                    this.broadcastMessageIncludingSelf(checkpoint);

                    // Log own checkpoint in accordance to hBFT 4.2
                    //messageLog.appendCheckpoint(checkpoint, this.tolerance, this.speculativeHistory, this.getViewNumber());
                } else if (seqNumber % this.messageLog.getCheckpointInterval() == 0) {
                    this.setTimeout(this::sendViewChangeOnTimeout, this.CHECKPOINT_TIMEOUT, "CHECKPOINT");
                }
            } else if (ticket.isCommittedConflicting(this.tolerance)) {
                ViewChangeMessage viewChangeMessage = this.messageLog.produceViewChange(this.getViewNumber() + 1, this.getViewNumber(), this.getId(), tolerance, this.speculativeRequests);
                this.sendViewChange(viewChangeMessage);
            }
        }
    }

    public void sendViewChangeOnTimeout() {
        ViewChangeMessage viewChangeMessage = this.messageLog.produceViewChange(this.getViewNumber() + 1, this.getViewNumber(), this.getId(), tolerance, this.speculativeRequests);
        this.sendViewChange(viewChangeMessage);
    }

    /**
     * Checks whether the replica already sent a reply.
     * We want to avoid sending redundant duplicate replies.
     * @param clientId The ID of the client.
     * @param timestamp The timestamp of the request.
     * @param currentViewNumber the current view number.
     * @param seqNumber the sequence number of the request.
     */
    private boolean replied(String clientId, long timestamp, long currentViewNumber, long seqNumber) {
        ReplicaRequestKey key = new ReplicaRequestKey(clientId, timestamp);
        Ticket<O, R> cachedTicket = messageLog.getTicketFromCache(key);
        if (cachedTicket != null) {
            return true;
        }
        Ticket<O, R> ticket = messageLog.getTicket(currentViewNumber, seqNumber);
        if (ticket != null) {
            return ticket.getReply() != null;
        }
        return false;
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
        this.sendReplyToClient(clientId, reply, this.tolerance, reply.getSequenceNumber());

        // When prior requests are fulfilled, attempt to process the buffer
        // to ensure they are dispatched in a timely manner
        this.handleNextBufferedRequest();
    }

    public void recvCheckpoint(CheckpointMessage checkpoint) {
        /*
         * Per hBFT 4.2, there is a 3 phase checkpoint subprotocol,
         * where the protocol starts with the primary sending Checkpoint-I message, 
         * then upon receiving this the replicas send a Checkpoint-II message, 
         * upon receiving 2f+1 Checkpoint-II messages it creates a CER1(M, v) and
         * replicas send Checkpoint-III messages, and upon receiving 2f+1 create CER2(M, v)
         * and perform GC if the checkpoint is stable.
         * 
         * If the replica recieves an incorrect CHECKPOINT-I message,
         * it sends a VIEW-CHANGE message. 
         */
        String primaryId = this.getPrimaryId();

        if (checkpoint instanceof CheckpointIMessage
         && (!primaryId.equals(checkpoint.getReplicaId())
         || !Arrays.equals(checkpoint.getDigest(), this.digest(this.speculativeHistory))
         || checkpoint.getLastSeqNumber() != this.seqCounter.get())) {
            System.out.println(checkpoint + " Replica: " + this.seqCounter.get() + " " + Arrays.equals(checkpoint.getDigest(), this.digest(this.speculativeHistory)));
            /* 
             * If the it is a CHECKPOINT-I message and not correct
             * then the replica sends a view-change.
             */
            this.checkpointForNewView = false;
            ViewChangeMessage viewChangeMessage = messageLog.produceViewChange(this.getViewNumber() + 1, this.getViewNumber(), this.getId(), tolerance, this.speculativeRequests);
            this.sendViewChange(viewChangeMessage);
        } else {
            // Upon receiving a checkpoint the replica gets out of disgruntled state
            this.disgruntled = false;

            // Clear the timeout for the checkpoint and panic
            this.clearSpecificTimeout("CHECKPOINT"); 
            this.clearSpecificTimeout("PANIC");
            this.messageLog.clearPanics();

            this.checkpointForNewView = false;
            this.messageLog.appendCheckpoint(checkpoint, this.tolerance, this.speculativeHistory, this.getViewNumber());

            if (checkpoint instanceof CheckpointIMessage) {            
                CheckpointMessage checkpointII = new CheckpointIIMessage(
                    checkpoint.getLastSeqNumber(),
                    this.digest(this.speculativeHistory),
                    this.getId(),
                    this.speculativeHistory
                );

                // I add it to the log as the sendCheckpoint doesnt include self
                messageLog.appendCheckpoint(checkpointII, this.tolerance, this.speculativeHistory, this.getViewNumber());
                this.sendCheckpoint(checkpointII);
            }

            if (checkpoint instanceof CheckpointIIMessage) { 
                // Check whether 2f + 1 Checkpoint-II messages have been seen
                // If yes then send Checkpoint-III
                boolean isCER1 = messageLog.isCER1(checkpoint, tolerance);
                if (isCER1) {
                    this.adjustHistory(checkpoint.getHistory().getRequests());
                    this.seqCounter.set(checkpoint.getLastSeqNumber());
                    CheckpointMessage checkpointIII = new CheckpointIIIMessage(
                        checkpoint.getLastSeqNumber(),
                        this.digest(this.speculativeHistory),
                        this.getId(),
                        this.speculativeHistory
                    );

                    // I add it to the log as the sendCheckpoint doesnt include self
                    messageLog.appendCheckpoint(checkpointIII, this.tolerance, this.speculativeHistory, this.getViewNumber());
                    this.sendCheckpoint(checkpointIII);
                }
            }

            if (checkpoint instanceof CheckpointIIIMessage) { 
                boolean isCER2 = messageLog.isCER2(checkpoint, tolerance);
                if (isCER2) {
                    this.adjustHistory(checkpoint.getHistory().getRequests());
                    this.seqCounter.set(checkpoint.getLastSeqNumber());
                    this.speculativeRequests.clear();
                }
            }

        }
    }

    public void adjustHistory(SortedMap<Long, RequestMessage> requests) {
        /* 
         * Commitlog requires the request to match,
         * so we add any missing requests.
         * Replicas also adjust their speculativeHistory as of
         * hBFT 4.4 (2): states a correct replica that received a 
         * bad request, learns the result and remain consistent
         */

        /* 
         * TODO: Probably some more logic is needed here, create ticket,
         * execute ticket, send reply etc.
         */
        for (Long key : requests.keySet()) {
            // All these requests need to be speculatively executed
            this.speculativeRequests.put(key, requests.get(key));
            if (!requests.get(key).equals(this.speculativeHistory.getHistory().get(key))) {
                this.speculativeHistory.addEntry(key, requests.get(key));
                
                // Add operation to commitLog
                Serializable operation = requests.get(key).getOperation();
                this.compute(key, new SerializableLogEntry(operation));
            }
        }
    }

    public void sendCheckpoint(CheckpointMessage checkpoint) {
        // hBFT 4.2 - Multicast checkpoint
        this.broadcastMessage(checkpoint);
    }

    public void enterNewView(long newViewNumber) {
        /*
         * Enter new view by resetting the disgruntled state, updating the
         * view number and clearing any prior timeouts that the replicas have
         * been waiting on
         */
        this.disgruntled = false;
        this.checkpointForNewView = true;
        this.setView(newViewNumber);
        this.clearAllTimeouts();
        this.messageLog.clearPanics();
        this.largestViewNumber = newViewNumber;
    }

    public void recvViewChange(ViewChangeMessage viewChange) {
        long curViewNumber = this.getViewNumber();
        long newViewNumber = viewChange.getNewViewNumber();
        String newPrimaryId = this.getRoundRobinPrimaryId(newViewNumber);
    
        // Checkpoint Synchronization: Make sure speculative history matches
        // if (!localDigest.equals(viewChange.getCheckpointDigest())) {
        //     this.sendViewChange(new ViewChangeMessage(newViewNumber, curViewNumber, localDigest, this.getId()));
        //     return;
        // }
    
        // hBFT: Process view change acceptance
        ViewChangeResult result = messageLog.acceptViewChange(viewChange, this.getId(), curViewNumber, this.tolerance);
        if (result.isShouldBandwagon()) {
            ViewChangeMessage bandwagonViewChange = messageLog.produceViewChange(
                result.getBandwagonViewNumber(),
                this.getViewNumber(),
                this.getId(),
                this.tolerance, this.speculativeRequests);
            this.sendViewChange(bandwagonViewChange);
        }


    
        // If this replica is the new primary, multicast NEW-VIEW and update state
        if (newPrimaryId.equals(this.getId())) {
            NewViewMessage newView = messageLog.produceNewView(newViewNumber, this.getId(), this.tolerance);
    
            if (newView != null) {
                /* 
                 * First the primary needs to execute the new-view
                 * only then can it send the correct checkpoint message
                 * 
                 * TODO: alternative solution is to send the checkpoint message based on the new-view itself
                 */
                this.recvNewView(newView);
                this.broadcastMessage(newView);
                this.checkpointForNewView = false;
                
                // as of hBFT 4.3 checkpoint protocol is called after a new-view message
                CheckpointMessage checkpoint = new CheckpointIMessage(seqCounter.get(), this.digest(this.speculativeHistory), this.getId(), this.speculativeHistory);
                this.messageLog.appendCheckpoint(checkpoint, tolerance, this.speculativeHistory, newView.getNewViewNumber());
                this.broadcastMessageIncludingSelf(checkpoint);
            }
        }
    }

    public void incrementViewChangeOnTimeout() {
        ViewChangeMessage viewChangeMessage = this.messageLog.produceViewChange(this.largestViewNumber + 1, this.getViewNumber(), this.getId(), tolerance, this.speculativeRequests);
        this.sendViewChange(viewChangeMessage);
    }

    public void sendViewChange(ViewChangeMessage viewChange) {
        this.disgruntled = true;
        this.largestViewNumber = viewChange.getNewViewNumber();
        // Restart the timeout
        this.clearSpecificTimeout("VIEW-CHANGE");
        this.setTimeout(this::incrementViewChangeOnTimeout, this.VIEWCHANGE_TIMEOUT, "VIEW-CHANGE");
        // hBFT 4.3 - Multicast VIEW-CHANGE vote
        this.broadcastMessage(viewChange);
    }

    public void recvNewView(NewViewMessage newView) {
        System.out.println(newView);
        /*
         * Probably non standard behaviour: hBFT does not state
         * what happens after a new view other than running a 
         * checkpoint sub-protocol. The replicas somehow need to
         * catch up with the history and verify the new view.
         * So I will do a similar step as in Zyzzyva, as
         * hBFT works very similar to it.
         * 
         * There are 3 possibilities:
         * 1. The replica is behind with its history
         * 2. The replica has different history than the other replicas
         * 3. The replica has the same history
         * 
         * Per hBFT 4.3 after a new view message is received,
         * the new primary needs to send a Checkpoint-I message
         * if this is not achieved, we can technically do two things
         * 1. Receive no other messages and wait for a timeout from the client.
         * 2. Initiate a view-change if another message is received.
         * 
         * For now I will choose option 1 and rely on the client.
         */
        if (!messageLog.acceptNewView(newView, this.tolerance)) {
            return;
        }

        long maxL = this.speculativeHistory.getGreatestSeqNumber();
        long minS = newView.getCheckpoint() == null ? 0 : newView.getCheckpoint().getSequenceNumber();
        long nextSeq = minS;

        // Case 1. - behind with history
        if (maxL < minS) { 
            for (Long seqNumOfHistory : newView.getSpeculativeHistory().getRequests().keySet()) {
                RequestMessage request = newView.getSpeculativeHistory().getRequests().get(seqNumOfHistory);

                if (maxL < seqNumOfHistory && seqNumOfHistory <= minS 
                    && request != null && !request.equals(this.speculativeHistory.getHistory().get(seqNumOfHistory))) {
                    this.speculativeHistory.addEntry(seqNumOfHistory, request);

                    // Given that these replicas have not commited the request yet
                    Serializable operation = request.getOperation();
                    this.compute(seqNumOfHistory, new SerializableLogEntry(operation));
                }
            }
            nextSeq = minS;
        // Case 2. - different history
        } else if (maxL >= minS 
            && this.digest(this.speculativeHistory.getHistoryBefore(maxL)) 
            != this.digest(newView.getSpeculativeHistory().getHistoryBefore(maxL))) {
                this.speculativeHistory.rollBack(minS);
                nextSeq = minS;
        // Case 3. - same history
        } else if (maxL >= minS 
            && this.digest(this.speculativeHistory.getHistoryBefore(maxL)) 
            == this.digest(newView.getSpeculativeHistory().getHistoryBefore(maxL))) {
                nextSeq = maxL;
        }

        this.seqCounter.set(nextSeq);

        // Replicas need to execute speculated requests
        for (Long seqNumOfHistory : newView.getSpeculativeHistory().getRequests().keySet()) {
            System.out.println("Replica " + this.getId() + " checking request with seqNum: " + seqNumOfHistory);
            if (seqNumOfHistory <= nextSeq) {
                continue;
            } else if (this.seqCounter.get() + 1 == seqNumOfHistory && newView.getSpeculativeHistory().getRequests().get(seqNumOfHistory) == null) {
                this.seqCounter.incrementAndGet();
                continue;
            }

            if (this.seqCounter.incrementAndGet() == seqNumOfHistory) {
                System.out.println("Replica " + this.getId() + " will execute with seqNum: " + seqNumOfHistory);
                this.executeRequestFromViewChange(newView.getSpeculativeHistory().getRequests().get(seqNumOfHistory), this.getViewNumber(), seqNumOfHistory);
            }
        }


        long newViewNumber = newView.getNewViewNumber();
        this.enterNewView(newViewNumber);
    }
    
    /* 
     * This is probably not the way to handle this,
     * but for now the replica executes the request given
     * that f + 1 other replicas executed it and sent a reply.
     * 
     * The replicas that already sent a reply make the request commited.
     */
    public void executeRequestFromViewChange(RequestMessage request, long currentViewNumber, long seqNumber) {
        if (request == null || !request.equals(this.speculativeHistory.getHistory().get(seqNumber))) {
            return;
        }

        Serializable operation = request.getOperation();
        Serializable result = this.compute(seqNumber, new SerializableLogEntry(operation));
    
        String clientId = request.getClientId();
        long timestamp = request.getTimestamp();

        // we have the set the local seqNumber to the prepare's sequence number
        this.seqCounter.set(seqNumber);

        this.speculativeRequests.put(seqNumber, request);
        this.speculativeHistory.addEntry(seqNumber, request);


        Ticket<O, R> ticket = messageLog.getTicket(currentViewNumber, seqNumber);
        if (ticket == null) {
            ticket = messageLog.newTicket(currentViewNumber, seqNumber);
        }
        ticket.append(request);

        // Clear the timeout for request completion
        this.clearSpecificTimeout("REQUEST");

        if (this.replied(request.getClientId(), request.getTimestamp(), currentViewNumber, seqNumber)) {
            ReplicaRequestKey key = new ReplicaRequestKey(clientId, timestamp);
            boolean completed = messageLog.completeTicket(key, currentViewNumber, seqNumber);
            System.out.println("Completed ticket: " + completed + " in view: " + currentViewNumber + " seq: " + seqNumber);
            return;
        }

        ReplyMessage reply = new ReplyMessage(
            currentViewNumber,
            timestamp,
            seqNumber,
            clientId,
            this.getId(),
            result,
            this.speculativeHistory);

        this.sendReply(clientId, reply);

        ticket.append(reply);
        ReplicaRequestKey key = new ReplicaRequestKey(clientId, timestamp);
        boolean completed = messageLog.completeTicket(key, currentViewNumber, seqNumber);
        System.out.println("Completed ticket: " + completed + " in view: " + currentViewNumber + " seq: " + seqNumber);
    }

    private String computePrimaryId(long viewNumber, int numReplicas) {
        List<String> knownReplicas = this.getNodeIds().stream().sorted().toList();
        return knownReplicas.get((int) viewNumber % numReplicas);
    }

    private String getPrimaryId() {
        return this.computePrimaryId(this.getViewNumber(), this.getNodeIds().size());
    }

    public Serializable compute(long sequenceNumber, LogEntry operation) {
        this.commitOperation(sequenceNumber, operation);
        return operation;
    }

    @Override
    public void handleClientRequest(String clientId, long timestamp, Serializable request) {
        // timestamp should be the time of the creation of the request
        RequestMessage m = new RequestMessage(request, timestamp, clientId);
        System.out.println(m);
        this.recvRequest(m);
    }

    @Override
    public void handleMessage(String sender, MessagePayload m) {
        if (m instanceof RequestMessage request) {
            System.out.println(request);
            recvRequest(request);
            return;
        } else if (m instanceof DefaultClientRequestPayload clientRequest) {
            System.out.println(clientRequest);
            handleClientRequest(sender, clientRequest.getTimestamp(), clientRequest.getOperation());
            return;
        } else if (m instanceof PrepareMessage prepare) {
            recvPrepare(prepare);
            return;
        } else if (m instanceof CommitMessage commit) {
            recvCommit(commit);
            return;
        } else if (m instanceof CheckpointMessage checkpoint) {
            recvCheckpoint(checkpoint);
            return;
        } else if (m instanceof ViewChangeMessage viewChange) {
            recvViewChange(viewChange);
            return;
        } else if (m instanceof NewViewMessage newView) {
            recvNewView(newView);
            return;
        } else if (m instanceof PanicMessage panic) {
            recvPanic(panic);
            return;
        }
        throw new RuntimeException("Unknown message type");
    }

    private boolean verifyPhase(long messageViewNumber, long messageSequenceNumber) {
        // TODO: WaterMarks: check if sequence number is within the window
        return messageViewNumber == this.getViewNumber();
    }

}
