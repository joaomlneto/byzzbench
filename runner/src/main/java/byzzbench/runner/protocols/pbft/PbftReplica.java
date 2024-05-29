package byzzbench.runner.protocols.pbft;

import byzzbench.runner.Replica;
import byzzbench.runner.protocols.pbft.message.*;
import byzzbench.runner.protocols.pbft.pojo.ReplicaRequestKey;
import byzzbench.runner.protocols.pbft.pojo.ReplicaTicketPhase;
import byzzbench.runner.protocols.pbft.pojo.ViewChangeResult;
import byzzbench.runner.state.TotalOrderCommitLog;
import byzzbench.runner.transport.MessagePayload;
import byzzbench.runner.transport.Transport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Log
@Serdeable
public class PbftReplica<O extends Serializable, R extends Serializable> extends Replica<Serializable> {

    @Getter
    private final int tolerance;

    @Getter
    private final long timeout;
    private final AtomicLong seqCounter = new AtomicLong(1);

    @Getter
    @JsonIgnore
    private final MessageLog messageLog;

    @JsonIgnore
    private final Map<ReplicaRequestKey, LinearBackoff> timeouts = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private volatile long viewNumber = 1;

    @Getter
    @Setter
    private volatile boolean disgruntled = false;

    public PbftReplica(String replicaId,
                       Set<String> nodeIds,
                       int tolerance,
                       long timeout,
                       MessageLog messageLog,
                       Transport transport) {
        super(replicaId, nodeIds, transport, new TotalOrderCommitLog());
        this.tolerance = tolerance;
        this.timeout = timeout;
        this.messageLog = messageLog;
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
            ReplyMessage reply = new ReplyMessage(
                    viewNumber,
                    timestamp,
                    clientId,
                    this.getNodeId(),
                    result);
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
         * accordance with PBFT 4.1.
         */
        ReplicaRequestKey key = new ReplicaRequestKey(clientId, timestamp);
        Ticket<O, R> cachedTicket = messageLog.getTicketFromCache(key);
        if (cachedTicket != null) {
            this.resendReply(clientId, cachedTicket);
            return;
        }

        // Start the timer for this request per PBFT 4.4
        this.timeouts.computeIfAbsent(key, k -> new LinearBackoff(this.viewNumber, this.timeout));

        String primaryId = this.getPrimaryId();

        // PBFT 4.1 - If the request is received by a non-primary replica
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

        long currentViewNumber = this.viewNumber;
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

        /*
         * Non-standard behavior - PBFT 4.2 specifies that requests are not to
         * be sent with PRE-PREPARE, but I leave it up to the transport to
         * decide how messages are sent and encoded and use the PRE-PREPARE
         * message to hold the request as well.
         *
         * Replica has accepted the request, multicast a PRE-PREPARE per PBFT
         * 4.2 which contains the view, the sequence number, request digest,
         * and the request message that was received.
         */
        PrePrepareMessage prePrepare = new PrePrepareMessage(
                currentViewNumber,
                seqNumber,
                this.digest(request),
                request);
        this.broadcastMessage(prePrepare);

        // PBFT 4.2 - Append PRE-PREPARE
        ticket.append(prePrepare);
    }

    public void recvRequest(RequestMessage request) {
        // PBFT 4.4 - Do not accept REQUEST when disgruntled
        if (this.disgruntled) {
            return;
        }

        // PBFT 4.2 - Attempt to process non-bufferred request
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

        long currentViewNumber = this.viewNumber;
        long viewNumber = message.getViewNumber();
        if (currentViewNumber != viewNumber) {
            return false;
        }

        long seqNumber = message.getSequenceNumber();
        return messageLog.isBetweenWaterMarks(seqNumber);
    }

    public void recvPrePrepare(PrePrepareMessage prePrepare) {
        if (!this.verifyPhaseMessage(prePrepare)) {
            return;
        }

        long currentViewNumber = this.viewNumber;
        byte[] digest = prePrepare.getDigest();
        RequestMessage request = prePrepare.getRequest();
        long seqNumber = prePrepare.getSequenceNumber();

        // PBFT 4.2 - Verify request digest
        byte[] computedDigest = this.digest(request);
        if (!Arrays.equals(digest, computedDigest)) {
            return;
        }

        /*
         * PBFT 4.2 specifies that given a valid PRE-PREPARE (matching
         * signature, view and valid sequence number), the replica must only
         * accept a PRE-PREPARE given that no other PRE-PREPARE has been
         * received OR that the new PRE-PREPARE matches the digest of the one
         * that was already received.
         *
         * Upon accepting the PRE-PREPARE, the replica adds it to the log and
         * multicasts a PREPARE to all other replicas and adding the PREPARE to
         * its log.
         */
        Ticket<O, R> ticket = messageLog.getTicket(currentViewNumber, seqNumber);
        if (ticket != null) {
            // PRE-PREPARE has previously been inserted into the log for this
            // sequence number - verify the digests match per PBFT 4.2
            for (Object message : ticket.getMessages()) {
                if (!(message instanceof PrePrepareMessage prevPrePrepare)) {
                    continue;
                }

                byte[] prevDigest = prevPrePrepare.getDigest();
                if (!Arrays.equals(prevDigest, digest)) {
                    return;
                }
            }
        } else {
            // PRE-PREPARE is the first - create a new ticket for it in this
            // replica (see #recvRequest(ReplicaRequest, boolean) for why the
            // message log is structured this way)
            ticket = messageLog.newTicket(currentViewNumber, seqNumber);
        }

        // PBFT 4.2 - Add PRE-PREPARE along with its REQUEST to the log
        ticket.append(prePrepare);

        // PBFT 4.2 - Multicast PREPARE to other replicas
        PrepareMessage prepare = new PrepareMessage(
                currentViewNumber,
                seqNumber,
                digest,
                this.getNodeId());
        this.broadcastMessage(prepare);

        // PBFT 4.2 - Add PREPARE to the log
        ticket.append(prepare);

        /*
         * Per PBFT 4.2, this replica stasfies the prepared predicate IF it has
         * valid PRE-PREPARE, REQUEST and PREPARE messages. Since processing is
         * done asynchronously, the replica state is checked when PRE-PREPARE
         * is accepted in case it arrives later than the corresponding PREPARE
         * messages.
         */
        this.tryAdvanceState(ticket, prePrepare);
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
        Ticket<O, R> ticket = this.recvPhaseMessage(prepare);

        if (ticket != null) {
            // PBFT 4.2 - Attempt to advance the state upon reception of
            // PREPARE to check if enough messages have been received to COMMIT
            this.tryAdvanceState(ticket, prepare);
        }
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
        if (phase == ReplicaTicketPhase.PRE_PREPARE) {
            /*
             * PRE_PREPARE state, formally prior to the prepared predicate
             * becomes true for this replica.
             *
             * Upon receiving 2*f (where f is the maximum faulty nodes) PREPARE
             * messages and validating them, attempt to CAS the phase to
             * indicate that the prepared predicate has become true and COMMIT
             * in accordance with PBFT 4.2.
             */
            if (ticket.isPrepared(this.tolerance) && ticket.casPhase(phase, ReplicaTicketPhase.PREPARE)) {
                CommitMessage commit = new CommitMessage(
                        currentViewNumber,
                        seqNumber,
                        digest,
                        this.getNodeId());
                this.broadcastMessage(commit);

                // PBFT 4.2 - Add own commit to the log
                ticket.append(commit);
            }
        }

        // Re-check the phase again to ensure that out-of-order COMMIT and
        // PREPARE messages do not prevent the replica from making progress
        phase = ticket.getPhase();

        if (phase == ReplicaTicketPhase.PREPARE) {
            /*
             * Per PBFT 4.2, committed-local is true only when committed is true
             * so the committed predicate is ignored. Committed-local is
             * achieved when 2*f + 1 COMMIT messages have been logged. CAS the
             * phase to indicate committed-local and perform the computation
             * synchronously.
             *
             * The computation may be performed asynchronously if the
             * implementer so wishes, as long as the cleanup is performed in a
             * callback of some sort.
             */
            if (ticket.isCommittedLocal(this.tolerance) && ticket.casPhase(phase, ReplicaTicketPhase.COMMIT)) {
                RequestMessage request = ticket.getRequest();
                if (request != null) {
                    Serializable operation = request.getOperation();
                    Serializable result = this.compute(operation);

                    String clientId = request.getClientId();
                    long timestamp = request.getTimestamp();
                    ReplyMessage reply = new ReplyMessage(
                            currentViewNumber,
                            timestamp,
                            clientId,
                            this.getNodeId(),
                            result);

                    ReplicaRequestKey key = new ReplicaRequestKey(clientId, timestamp);
                    messageLog.completeTicket(key, currentViewNumber, seqNumber);
                    this.sendReply(clientId, reply);

                    this.timeouts.remove(key);
                }

                /*
                 * Checkpointing is specified by PBFT 4.3. A checkpoint is
                 * reached every time the sequence number mod the interval
                 * reaches 0, in which case a CHECKPOINT message is sent
                 * containing the current sequence number, the digest of the
                 * current state and this replica's ID.
                 *
                 * This replica implementation is stateless with respect to the
                 * requests made by the client (i.e. clients do not change the
                 * state of the replica, only the state of the protocol), and so
                 * the digest method simply returns an empty array.
                 */
                if (seqNumber % messageLog.getCheckpointInterval() == 0) {
                    CheckpointMessage checkpoint = new CheckpointMessage(
                            seqNumber,
                            this.digest(this),
                            this.getNodeId());
                    this.sendCheckpoint(checkpoint);

                    // Log own checkpoint in accordance to PBFT 4.3
                    messageLog.appendCheckpoint(checkpoint, this.tolerance);
                }
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
        this.sendMessage(reply, clientId);

        // When prior requests are fulfilled, attempt to process the buffer
        // to ensure they are dispatched in a timely manner
        this.handleNextBufferedRequest();
    }

    public void recvCheckpoint(CheckpointMessage checkpoint) {
        /*
         * Per PBFT 4.3, upon reception of a checkpoint message, check for
         * 2*f + 1 checkpoint messages and perform GC if the checkpoint is
         * stable.
         */
        messageLog.appendCheckpoint(checkpoint, this.tolerance);
    }

    public void sendCheckpoint(CheckpointMessage checkpoint) {
        // PBFT 4.3 - Multicast checkpoint
        this.broadcastMessage(checkpoint);
    }

    private void enterNewView(long newViewNumber) {
        /*
         * Enter new view by resetting the disgruntled state, updating the
         * view number and clearing any prior timeouts that the replicas have
         * been waiting on
         */
        this.disgruntled = false;
        this.viewNumber = newViewNumber;
        this.timeouts.clear();
    }

    public void recvViewChange(ViewChangeMessage viewChange) {
        /*
         * A replica sends a view change to vote out the primary when it
         * becomes disgruntled due to a (or many) timeouts in accordance with
         * PBFT 4.4.
         *
         * When the the view change is recorded to the message log, it will
         * determine firstly whether it should bandwagon the next view change
         * and then secondly whether it should start the next view change
         * timer in accordance with PBFT 4.5.2.
         */
        long curViewNumber = this.viewNumber;
        long newViewNumber = viewChange.getNewViewNumber();
        String newPrimaryId = this.computePrimaryId(newViewNumber, this.getNodeIds().size());

        // PBFT 4.5.2 - Determine whether to bandwagon on the lowest view change
        ViewChangeResult result = messageLog.acceptViewChange(viewChange,
                this.getNodeId(),
                curViewNumber,
                this.tolerance);
        if (result.isShouldBandwagon()) {
            ViewChangeMessage bandwagonViewChange = messageLog.produceViewChange(
                    result.getBandwagonViewNumber(),
                    this.getNodeId(),
                    this.tolerance);
            this.sendViewChange(bandwagonViewChange);
        }

        // PBFT 4.5.2 - Start the timers that will vote for newViewNumber + 1.
        if (result.isBeginNextVote()) {
            for (LinearBackoff backoff : this.timeouts.values()) {
                synchronized (backoff) {
                    long timerViewNumber = backoff.getNewViewNumber();
                    if (newViewNumber + 1 == timerViewNumber) {
                        backoff.beginNextTimer();
                    }
                }
            }
        }

        if (newPrimaryId.equals(this.getNodeId())) {
            /*
             * Per PBFT 4.4, if this is the replica being voted as the new
             * primary, then when it receives 2*f votes, it will multicast a
             * NEW-VIEW message to notify the other replicas and perform the
             * necessary procedures to enter the new view.
             */
            NewViewMessage newView = messageLog.produceNewView(newViewNumber, this.getNodeId(), this.tolerance);
            if (newView != null) {
                this.broadcastMessage(newView);

                /*
                 * Possibly non-standard behavior - there is actually no
                 * mention of sequence number synchronization, but clearly this
                 * must be done because the NEW-VIEW may possibly update the
                 * checkpoint past the current sequence number; therefore, the
                 * new primary has to ensure that any subsequent requests being
                 * dispatched after the view change occurs must have a
                 * sufficiently high sequence number to pass the watermark test.
                 */
                Collection<PrePrepareMessage> preparedProofs = newView.getPreparedProofs();
                for (PrePrepareMessage prePrepare : preparedProofs) {
                    long seqNumber = prePrepare.getSequenceNumber();
                    if (this.seqCounter.get() < seqNumber) {
                        this.seqCounter.set(seqNumber + 1);
                    }
                }

                this.enterNewView(newViewNumber);
            }
        }
    }

    public void sendViewChange(ViewChangeMessage viewChange) {
        // PBFT 4.4 - Multicast VIEW-CHANGE vote
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

    public Serializable compute(Serializable operation) {
        this.commitOperation(operation);
        return operation;
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
