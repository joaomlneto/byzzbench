package byzzbench.simulator.protocols.hbft;

import byzzbench.simulator.protocols.hbft.message.*;
import byzzbench.simulator.protocols.hbft.pojo.ReplicaRequestKey;
import byzzbench.simulator.protocols.hbft.pojo.TicketKey;
import byzzbench.simulator.protocols.hbft.pojo.ViewChangeResult;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageLog implements Serializable {
    private static final byte[] NULL_DIGEST = new byte[0];
    private static final RequestMessage NULL_REQ = new RequestMessage(null, 0, "");

    private final int bufferThreshold;
    @Getter
    private final int checkpointInterval;
    @Getter
    private final int watermarkInterval;

    private final Deque<RequestMessage> buffer = new ConcurrentLinkedDeque<>();

    private final SortedMap<ReplicaRequestKey, Ticket<?, ?>> ticketCache = new TreeMap<>();
    private final SortedMap<TicketKey, Ticket<?, ?>> tickets = new TreeMap<>();

    private final SortedMap<Long, Collection<CheckpointMessage>> checkpointsI = new TreeMap<>();
    private final SortedMap<Long, Collection<CheckpointMessage>> checkpointsII = new TreeMap<>();
    private final SortedMap<Long, Collection<CheckpointMessage>> checkpointsIII = new TreeMap<>();
    private final SortedMap<Long, SortedMap<String, ViewChangeMessage>> viewChanges = new TreeMap<>();

    private SpeculativeHistory historyOfCheckpointI;
    private SpeculativeHistory certificate1;
    private SpeculativeHistory certificate2;

    // Feel like this is a better way of storing the certificates
    // (seqNumber, speculativeHistory)
    // private SortedMap<Long, Collection<SpeculativeHistory>> cer1 = new TreeMap<>();
    // private SortedMap<Long, Collection<SpeculativeHistory>> cer2 = new TreeMap<>();

    private volatile long lowWaterMark;
    private volatile long highWaterMark;

    public MessageLog(int bufferThreshold, int checkpointInterval, int watermarkInterval) {
        this.bufferThreshold = bufferThreshold;
        this.checkpointInterval = checkpointInterval;
        this.watermarkInterval = watermarkInterval;

        this.lowWaterMark = 0;
        this.highWaterMark = this.lowWaterMark + watermarkInterval;
    }

    public <O extends Serializable, R extends Serializable> Ticket<O, R> getTicketFromCache(ReplicaRequestKey key) {
        //return (Ticket<O, R>) this.ticketCache.get(key);

        if (this.ticketCache.containsKey(key)) {
            return (Ticket<O, R>) this.ticketCache.get(key);
        } else {
            return null;
        }
    }

    public <O extends Serializable, R extends Serializable> Ticket<O, R> getTicket(long viewNumber, long seqNumber) {
        TicketKey key = new TicketKey(viewNumber, seqNumber);
        return (Ticket<O, R>) this.tickets.get(key);
    }

    public @NonNull <O extends Serializable, R extends Serializable> Ticket<O, R> newTicket(long viewNumber, long seqNumber) {
        TicketKey key = new TicketKey(viewNumber, seqNumber);
        return (Ticket<O, R>) this.tickets.computeIfAbsent(key, k -> new Ticket<>(viewNumber, seqNumber));
    }

    public boolean completeTicket(ReplicaRequestKey rrk, long viewNumber, long seqNumber) {
        TicketKey key = new TicketKey(viewNumber, seqNumber);
        Ticket<?, ?> ticket = this.tickets.remove(key);

        this.ticketCache.put(rrk, ticket);

        return ticket != null;
    }

    private void gcCheckpoint(long checkpoint) {
        /*
         * Procedure used to discard all CHECKPOINT, PREPARE, COMMIT, REQUEST and REPLY
         * messages with sequence number less than or equal the in addition to
         * any prior checkpoint proof per hBFT 4.2.
         *
         * A stable checkpoint then allows the water marks to slide over to
         * the checkpoint < x <= checkpoint + watermarkInterval per hBFT 4.2.
         */
        for (Map.Entry<ReplicaRequestKey, Ticket<?, ?>> entry : this.ticketCache.entrySet()) {
            Ticket<?, ?> ticket = entry.getValue();
            if (ticket.getSeqNumber() <= checkpoint) {
                this.ticketCache.remove(entry.getKey());
            }
        }

        for (Long seqNumber : this.checkpointsI.keySet()) {
            if (seqNumber < checkpoint) {
                this.checkpointsI.remove(seqNumber);
            }
        }

        for (Long seqNumber : this.checkpointsII.keySet()) {
            if (seqNumber < checkpoint) {
                this.checkpointsII.remove(seqNumber);
            }
        }

        for (Long seqNumber : this.checkpointsIII.keySet()) {
            if (seqNumber < checkpoint) {
                this.checkpointsIII.remove(seqNumber);
            }
        }

        this.highWaterMark = checkpoint + this.watermarkInterval;
        this.lowWaterMark = checkpoint;
    }

    public void appendCheckpoint(CheckpointMessage checkpoint, int tolerance, SpeculativeHistory history) {
        /*
         * Per hBFT 4.2, each time a checkpoint-III is generated or received, it
         * gets stored in the log until 2f + 1 are accumulated (CER2(M,v)) that have
         * matching digests to the checkpoint that was added to the log, in
         * which case the garbage collection occurs (see #gcCheckpoint(long)).
         */
        long seqNumber = checkpoint.getLastSeqNumber();

        if (checkpoint instanceof CheckpointIMessage) {
            Collection<CheckpointMessage> checkpointProofs = this.checkpointsI.computeIfAbsent(seqNumber, k -> new ConcurrentLinkedQueue<>());
            checkpointProofs.add(checkpoint);
            this.historyOfCheckpointI = history;
        }

        if (checkpoint instanceof CheckpointIIMessage) {
            Collection<CheckpointMessage> checkpointProofs = this.checkpointsII.computeIfAbsent(seqNumber, k -> new ConcurrentLinkedQueue<>());
            checkpointProofs.add(checkpoint);
        }

        if (checkpoint instanceof CheckpointIIIMessage) {
            Collection<CheckpointMessage> checkpointProofs = this.checkpointsIII.computeIfAbsent(seqNumber, k -> new ConcurrentLinkedQueue<>());
            checkpointProofs.add(checkpoint);

            final int stableCount = 2 * tolerance + 1;
            int matching = 0;

            // Use a loop here to avoid the linked list being traversed in its
            // entirety
            for (CheckpointMessage proof : checkpointProofs) {
                if (Arrays.equals(proof.getDigest(), checkpoint.getDigest())) {
                    matching++;

                    if (matching == stableCount) {
                        this.certificate2 = history;
                        this.gcCheckpoint(seqNumber);
                        return;
                    }
                }
            }
        }
    }

    public boolean isCER1(CheckpointMessage checkpoint, int tolerance, SpeculativeHistory history) {
        long seqNumber = checkpoint.getLastSeqNumber();
        Collection<CheckpointMessage> checkpointProofs = this.checkpointsII.computeIfAbsent(seqNumber, k -> new ConcurrentLinkedQueue<>());
        
        final int stableCount = 2 * tolerance + 1;
        int matching = 0;

        // Use a loop here to avoid the linked list being traversed in its
        // entirety
        for (CheckpointMessage proof : checkpointProofs) {
            if (Arrays.equals(proof.getDigest(), checkpoint.getDigest())) {
                matching++;

                if (matching == stableCount) {
                    this.certificate1 = history;
                    return true;
                }
            }
        }

        return false;
    }

    public ViewChangeMessage produceViewChange(long newViewNumber, String replicaId, int tolerance, SpeculativeHistory history) {
        /*
         * Produces a VIEW-CHANGE vote message in accordance with hBFT 4.3.
         *
         * The last stable checkpoint is defined as the low water mark for the
         * message log.
         */
        long checkpoint = this.lowWaterMark;

        Collection<CheckpointMessage> checkpointProofs = checkpoint == 0 ?
                Collections.emptyList() : this.checkpointsIII.get(checkpoint);
        if (checkpointProofs == null) {
            throw new IllegalStateException("Checkpoint has diverged without any proof");
        }

        /* 
         * Speculatively executed requests with sequence number higher,
         * than the last accepted checkpoint (lowWaterMark)
         */
        SpeculativeHistory filteredHistory = history.getHistory(lowWaterMark);

        ViewChangeMessage viewChange = new ViewChangeMessage(
            newViewNumber,
            certificate1,
            historyOfCheckpointI,
            filteredHistory,
            replicaId);

        /*
         * Potentially non-standard behavior - PBFT 4.5.2 does not specify
         * whether replicas include their own view change messages. For 3f + 1
         * replicas in the system, then given the max f faulty nodes, 3f + 1 - f
         * or 2f + 1 replicas are expected to vote, meaning that excluding the
         * initiating replica reduces the total number of votes to 2f. Since
         * PBFT 4.5.2 states that the next view change may only be initiated by
         * a quorum of 2f + 1 replicas, then electing a faulty primary that does
         * not multicast a NEW-VIEW message will cause the entire system to
         * stall; therefore I do include the initiating replica here.
         */
        SortedMap<String, ViewChangeMessage> newViewSet = this.viewChanges.computeIfAbsent(newViewNumber, k -> new TreeMap<>());
        newViewSet.put(replicaId, viewChange);

        return viewChange;
    }

    public ViewChangeResult acceptViewChange(ViewChangeMessage viewChange, String curReplicaId, long curViewNumber, int tolerance) {
        /*
         * Per PBFT 4.4, a received VIEW-CHANGE vote is stored into the message
         * log and the state is returned to the replica as
         * ReplicaViewChangeResult.
         *
         * The procedure first computes the total number of votes from other
         * replicas that try to move the view a higher view number. If this
         * number of other relicas is equal to the bandwagon size, then this
         * replica contributes its vote once to avoid creating an infinite
         * response loop and taking up the network capacity.
         *
         * Secondly, this procedure finds the smallest view the system is
         * attempting to elect and selects that to bandwagon.
         *
         * Finally, this procedure determines if the number of votes is enough
         * to restart the timer to move to the view after the one now being
         * elected in the case that the candidate view has a faulty primary.
         */
        long newViewNumber = viewChange.getNewViewNumber();
        String replicaId = viewChange.getReplicaId();

        SortedMap<String, ViewChangeMessage> newViewSet = this.viewChanges.computeIfAbsent(newViewNumber, k -> new TreeMap<>());
        newViewSet.put(replicaId, viewChange);

        final int bandwagonSize = tolerance + 1;

        int totalVotes = 0;
        long smallestView = Long.MAX_VALUE;
        for (SortedMap.Entry<Long, SortedMap<String, ViewChangeMessage>> entry : this.viewChanges.entrySet()) {
            long entryView = entry.getKey();
            if (entryView <= curViewNumber) {
                continue;
            }

            SortedMap<String, ViewChangeMessage> votes = entry.getValue();
            int entryVotes = votes.size();

            /*
             * See #produceViewChange(...)
             * Subtract the current replica's vote to obtain the votes from the
             * other replicas
             */
            if (votes.containsKey(curReplicaId)) {
                entryVotes--;
            }

            totalVotes += entryVotes;

            if (smallestView > entryView) {
                smallestView = entryView;
            }
        }

        boolean shouldBandwagon = totalVotes == bandwagonSize;

        final int timerThreshold = 2 * tolerance + 1;
        boolean beginNextVote = newViewSet.size() >= timerThreshold;

        return new ViewChangeResult(shouldBandwagon, smallestView, beginNextVote);
    }

    public NewViewMessage produceNewView(long newViewNumber, String replicaId, int tolerance) {
        /*
         * Produces the NEW-VIEW message to notify the other replicas of the
         * elected primary in accordance with PBFT 4.4.
         */

        SortedMap<String, ViewChangeMessage> newViewSet = this.viewChanges.get(newViewNumber);
        int votes = newViewSet.size();
        boolean hasOwnViewChange = newViewSet.containsKey(replicaId);
        if (hasOwnViewChange) {
            votes--;
        }

        final int quorum = 2 * tolerance;
        if (votes < quorum) {
            return null;
        }

        // Rule A: Find a valid execution history M from the view-change messages
        SpeculativeHistory selectedHistoryM = null;
        Collection<CheckpointMessage> checkpointProofs = null;
        long minSeqNum = Long.MAX_VALUE;
        long maxSeqNum = Long.MIN_VALUE;

        long certificateTolerance = 2 * tolerance + 1;

        for (ViewChangeMessage viewChange : newViewSet.values()) {
            SpeculativeHistory speculativeP = viewChange.getSpeculativeHistoryP();

            // Rule A1: Check if speculative history M has CER1(M, v) from at least 2f + 1 replicas
            if (isValidExecutionHistory(speculativeP, tolerance)) {
                selectedHistoryM = speculativeP;
                checkpointProofs = this.getCheckpointProofs(viewChange);
                break;
            }
        }

        // Rule B: If no history was found in the view changes, select last stable checkpoint
        if (selectedHistoryM == null) {
            selectedHistoryM = this.getLastStableCheckpoint();
        }

        // Collect the required view-change proofs for the new view
        Collection<ViewChangeMessage> viewChangeProofs = new ArrayList<>(newViewSet.values());
        if (!hasOwnViewChange) {
            viewChangeProofs.add(this.produceViewChange(newViewNumber, replicaId, tolerance));
        }


        // Construct the New-View message with V, X (selected checkpoint), and M (speculative history)
        return new NewViewMessage(
                newViewNumber,
                viewChangeProofs,
                selectedHistoryM);
    }

    private void gcNewView(long newViewNumber) {
        /*
         * Performs clean-up for entering a new view in accordance with PBFT
         * 4.4. This means that any view change votes and pending tickets that
         * are not in the new view are removed.
         */
        this.viewChanges.remove(newViewNumber);

        for (TicketKey key : this.tickets.keySet()) {
            if (key.getViewNumber() != newViewNumber) {
                this.tickets.remove(key);
            }
        }
    }

    public boolean acceptNewView(NewViewMessage newView) {
        /*
         * Verify the change to a new view in accordance with PBFT 4.4 and then
         * find the min-s value and update the low water mark if it is lagging
         * behind the new view.
         */
        long newViewNumber = newView.getNewViewNumber();
        this.gcNewView(newViewNumber);

        long minS = Integer.MAX_VALUE;
        Collection<CheckpointMessage> checkpointProofs = null;
        Collection<ViewChangeMessage> viewChangeProofs = newView.getViewChangeProofs();
        for (ViewChangeMessage viewChange : viewChangeProofs) {
            if (newViewNumber != viewChange.getNewViewNumber()) {
                return false;
            }

            long seqNumber = viewChange.getLastSeqNumber();
            if (seqNumber < minS) {
                minS = seqNumber;
                checkpointProofs = viewChange.getCheckpointProofs();
            }
        }

        if (this.lowWaterMark < minS) {
            this.checkpoints.put(minS, checkpointProofs);
            this.gcCheckpoint(minS);
        }

        return true;
    }

    public boolean shouldBuffer() {
        return this.tickets.size() >= this.bufferThreshold;
    }

    public <O> void buffer(RequestMessage request) {
        this.buffer.addLast(request);
    }

    public <O> RequestMessage popBuffer() {
        return this.buffer.pollFirst();
    }

    public boolean isBetweenWaterMarks(long seqNumber) {
        return seqNumber >= this.lowWaterMark && seqNumber <= this.highWaterMark;
    }
}
