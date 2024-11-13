package byzzbench.simulator.protocols.hbft;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import byzzbench.simulator.protocols.hbft.message.CheckpointIIIMessage;
import byzzbench.simulator.protocols.hbft.message.CheckpointIIMessage;
import byzzbench.simulator.protocols.hbft.message.CheckpointIMessage;
import byzzbench.simulator.protocols.hbft.message.CheckpointMessage;
import byzzbench.simulator.protocols.hbft.message.NewViewMessage;
import byzzbench.simulator.protocols.hbft.message.RequestMessage;
import byzzbench.simulator.protocols.hbft.message.ViewChangeMessage;
import byzzbench.simulator.protocols.hbft.pojo.ReplicaRequestKey;
import byzzbench.simulator.protocols.hbft.pojo.TicketKey;
import byzzbench.simulator.protocols.hbft.pojo.ViewChangeResult;
import byzzbench.simulator.protocols.hbft.utils.Checkpoint;
import lombok.Getter;
import lombok.NonNull;


public class MessageLog implements Serializable {
    //private static final byte[] NULL_DIGEST = new byte[0];
    //private static final RequestMessage NULL_REQ = new RequestMessage(null, 0, "");

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
    // Stored as (viewNumber, (replicaId, message))
    private final SortedMap<Long, SortedMap<String, ViewChangeMessage>> viewChanges = new TreeMap<>();

    private Checkpoint lastStableCheckpoint;

    private Checkpoint lastAcceptedCheckpointI;
    private SortedMap<Long, Collection<SpeculativeHistory>> cer1 = new TreeMap<>();
    private SortedMap<Long, Collection<SpeculativeHistory>> cer2 = new TreeMap<>();

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

    private void gcCheckpoint(long checkpoint, SpeculativeHistory history) {
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

        // Reset the certificates
        // TODO: I am not sure whether these should be cleared or not
        this.cer1 = new TreeMap<>();
        this.cer2 = new TreeMap<>();
        this.lastAcceptedCheckpointI = null;

        this.lastStableCheckpoint = new Checkpoint(checkpoint, history);

        this.highWaterMark = checkpoint + this.watermarkInterval;
        this.lowWaterMark = checkpoint;
    }

    public void appendCheckpoint(CheckpointMessage checkpoint, int tolerance, SpeculativeHistory history, long viewNumber) {
        /*
         * Per hBFT 4.2, each time a checkpoint-III is generated or received, it
         * gets stored in the log until 2f + 1 are accumulated (CER2(M,v)) that have
         * matching digests to the checkpoint that was added to the log, in
         * which case the garbage collection occurs (see #gcCheckpoint(long))
         * and a stable checkpoint is established.
         */
        long seqNumber = checkpoint.getLastSeqNumber();

        if (checkpoint instanceof CheckpointIMessage) {
            Collection<CheckpointMessage> checkpointProofs = this.checkpointsI.computeIfAbsent(seqNumber, k -> new ConcurrentLinkedQueue<>());
            checkpointProofs.add(checkpoint);
            this.lastAcceptedCheckpointI = new Checkpoint(seqNumber, history);
        }

        if (checkpoint instanceof CheckpointIIMessage) {
            Collection<CheckpointMessage> checkpointProofs = this.checkpointsII.computeIfAbsent(seqNumber, k -> new ConcurrentLinkedQueue<>());
            checkpointProofs.add(checkpoint);

            // Can produce duplicates
            Collection<SpeculativeHistory> cer1History = this.cer1.computeIfAbsent(viewNumber, k -> new ConcurrentLinkedQueue<>());
            cer1History.add(history);
        }

        if (checkpoint instanceof CheckpointIIIMessage) {
            Collection<CheckpointMessage> checkpointProofs = this.checkpointsIII.computeIfAbsent(seqNumber, k -> new ConcurrentLinkedQueue<>());
            checkpointProofs.add(checkpoint);

            // Can produce duplicates
            Collection<SpeculativeHistory> cer2History = this.cer2.computeIfAbsent(viewNumber, k -> new ConcurrentLinkedQueue<>());
            cer2History.add(history);

            final int stableCount = 2 * tolerance + 1;
            int matching = 0;

            for (CheckpointMessage proof : checkpointProofs) {
                if (Arrays.equals(proof.getDigest(), checkpoint.getDigest())) {
                    matching++;

                    if (matching == stableCount) {
                        this.gcCheckpoint(seqNumber, history);
                        return;
                    }
                }
            }
        }
    }

    public boolean isCER1(CheckpointMessage checkpoint, int tolerance, SpeculativeHistory history) {
        long seqNumber = checkpoint.getLastSeqNumber();
        // Should there be at least one because we add the checkpoint before calling this function
        Collection<CheckpointMessage> checkpointProofs = this.checkpointsII.computeIfAbsent(seqNumber, k -> new ConcurrentLinkedQueue<>());
        
        final int stableCount = 2 * tolerance + 1;
        int matching = 0;

        for (CheckpointMessage proof : checkpointProofs) {
            if (Arrays.equals(proof.getDigest(), checkpoint.getDigest())) {
                matching++;

                if (matching == stableCount) {
                    return true;
                }
            }
        }

        return false;
    }

    /*
     * Checks whether a collection is empty
     */
    public boolean isNullOrEmpty( final Collection< ? > c ) {
        return c == null || c.isEmpty();
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
        SpeculativeHistory requestsR = history.getHistory(checkpoint);

        /* 
         * Execution history from previous view
         * CER1(M, v-1)
         */
        SpeculativeHistory historyP;
        if (!this.isNullOrEmpty(this.cer1.get(newViewNumber - 1))) {
            historyP = this.cer1.get(newViewNumber - 1).iterator().next();
        } else {
            historyP = null;
        }

        /* 
         * Q execution history from the accepted Checkpoint-I message
         * TODO: figure out which view is this
         */
        SpeculativeHistory historyQ;
        if (this.lastAcceptedCheckpointI != null) {
            historyQ = this.lastAcceptedCheckpointI.getHistory();
        } else {
            historyQ = null;
        }

        // cer1 could be empty
        ViewChangeMessage viewChange = new ViewChangeMessage(
            newViewNumber,
            historyP,
            historyQ,
            requestsR.getRequests(),
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
         * Per hBFT 4.3, a received VIEW-CHANGE vote is stored into the message
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
         * elected primary in accordance with hBFT 4.3.
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

        boolean ruleASatisfied = false;

        // Rule A: Find a valid execution history M from the view-change messages
        SpeculativeHistory selectedHistoryM = null;
        long certificateTolerance = 2 * tolerance + 1;
        Map<SpeculativeHistory, Integer> historyMap = new HashMap<>();

        // Replica needs all the possibly executed requests for later
        Map<Long, Integer> requestMap = new HashMap<>();
        Collection<SortedMap<Long, RequestMessage>> allRequests = new ArrayList<>();

        // Rule A1: Check if speculative history M has CER1(M, v) from at least 2f + 1 replicas
        // Loop through the view-change messages and try to find 2f + 1 matching P history
        for (ViewChangeMessage viewChangePerReplica : newViewSet.values()) {
            SpeculativeHistory pHistory = viewChangePerReplica.getSpeculativeHistoryP();
            SortedMap<Long, RequestMessage> requests = viewChangePerReplica.getRequestsR();
            allRequests.add(requests);

            for (long seqNum : requests.keySet()) {
                requestMap.put(seqNum, requestMap.getOrDefault(seqNum, 0) + 1);
            }
            
            // We tackle the empty history later in rule B
            if (pHistory == null || pHistory.isEmpty()) {
                continue;
            }

            historyMap.put(pHistory, historyMap.getOrDefault(pHistory, 0) + 1);
        }

        for (Map.Entry<SpeculativeHistory, Integer> entry : historyMap.entrySet()) {
            if (entry.getValue() >= certificateTolerance) {
                selectedHistoryM = entry.getKey();
            }
        }

        // Rule A2: At least f + 1 replicas accepted Checkpoint-I in view v' > v
        if (selectedHistoryM != null) {
            final int stableCount = tolerance + 1;
            int matching = 0;

            for (Long key : this.cer1.keySet()) {
                if (key > newViewNumber - 1) {
                    matching += this.cer1.get(key).size();
                }

                if (matching >= stableCount) {
                    // Get first element, should all be the same history
                    ruleASatisfied = true;
                    break;
                }
            }
        }
        
        // If rule A fails, primary tries rule B
        if (!ruleASatisfied) {
            int counter = 0;
            for (ViewChangeMessage viewChangePerReplica : newViewSet.values()) {
                SpeculativeHistory pHistory = viewChangePerReplica.getSpeculativeHistoryP();

                if (pHistory == null || pHistory.isEmpty()) {
                    counter++;
                }
            }

            if (counter >= certificateTolerance) {
                if (this.lastStableCheckpoint == null) {
                    selectedHistoryM = null;
                } else {
                    selectedHistoryM = this.lastStableCheckpoint.getHistory();
                }
            } else {
                // TODO: Either fallback or start a new view-change protocol
            }
        }

        SpeculativeHistory sortedRequests = new SpeculativeHistory();

        /*  
         * Select every request in R if f+1 replicas include it
         * And sequence number is greater than the largest in selectedHistoryM
         */
        for (SortedMap<Long, RequestMessage> requests : allRequests) {
            for (Map.Entry<Long, RequestMessage> request : requests.entrySet()) {
                if ((selectedHistoryM == null || request.getKey() > selectedHistoryM.getGreatestSeqNumber()) && requestMap.get(request.getKey()) >= tolerance + 1) {
                    sortedRequests.addEntry(request.getKey(), request.getValue());
                }
            }
        }

        if (selectedHistoryM != null) {
            SortedMap<Long, RequestMessage> historyMRequests = selectedHistoryM.getRequests();
            for (long seqNumber : selectedHistoryM.getRequests().keySet()) {
                sortedRequests.addEntry(seqNumber, historyMRequests.get(seqNumber));
            }
        }

        /*  
         * If Rule A is correct then the selected Checkpoint
         * is the selectedHistoryM.
         * Otherwise its the replicas last stable checkpoint.
         * Nullable if there is no previous checkpoint.
         */
        Checkpoint selectedCheckpoint;
        if (selectedHistoryM == null) {
            selectedCheckpoint = this.lastStableCheckpoint;
        } else {
            selectedCheckpoint = new Checkpoint(selectedHistoryM.getGreatestSeqNumber(), selectedHistoryM);
        }

        // Construct the New-View message with V, X (selected checkpoint), and M (speculative history)
        return new NewViewMessage(
            newViewNumber,
            newViewSet.values(),
            selectedCheckpoint,
            sortedRequests);
}

    private void gcNewView(long newViewNumber) {
        /*
         * hBFT clean up after new view is similar as of PBFT
         * 
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
