package byzzbench.simulator.protocols.hbft;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
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
import byzzbench.simulator.protocols.hbft.message.PanicMessage;
import byzzbench.simulator.protocols.hbft.message.RequestMessage;
import byzzbench.simulator.protocols.hbft.message.ViewChangeMessage;
import byzzbench.simulator.protocols.hbft.pojo.ClientRequestKey;
import byzzbench.simulator.protocols.hbft.pojo.ReplicaRequestKey;
import byzzbench.simulator.protocols.hbft.pojo.TicketKey;
import byzzbench.simulator.protocols.hbft.pojo.ViewChangeResult;
import byzzbench.simulator.protocols.hbft.utils.Checkpoint;
import byzzbench.simulator.protocols.hbft.utils.ScheduleLogger;
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
    @Getter
    private final SortedMap<TicketKey, Ticket<?, ?>> tickets = new TreeMap<>();

    private final SortedMap<Long, SortedMap<String, CheckpointMessage>> checkpointsI = new TreeMap<>();
    private final SortedMap<Long, SortedMap<String, CheckpointMessage>> checkpointsII = new TreeMap<>();
    private final SortedMap<Long, SortedMap<String, CheckpointMessage>> checkpointsIII = new TreeMap<>();
    // Stored as (viewNumber, (replicaId, message))
    @Getter
    private final SortedMap<Long, SortedMap<String, ViewChangeMessage>> viewChanges = new TreeMap<>();

    // Panic messages from replicas
    @Getter
    private final SortedMap<String, PanicMessage> panics = new TreeMap<>();

    @Getter
    private Checkpoint lastStableCheckpoint = new Checkpoint(0, null);

    // Stored as (viewNumber, Checkpoint)
    private SortedMap<Long, Checkpoint> acceptedCheckpointIs = new TreeMap<>();

    private final SortedMap<Long, Collection<SpeculativeHistory>> cer1 = new TreeMap<>();
    private final SortedMap<Long, Collection<SpeculativeHistory>> cer2 = new TreeMap<>();

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
        Iterator<Map.Entry<ReplicaRequestKey, Ticket<?, ?>>> iterator = this.ticketCache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ReplicaRequestKey, Ticket<?, ?>> entry = iterator.next();
            Ticket<?, ?> ticket = entry.getValue();
            if (ticket.getSeqNumber() <= checkpoint) {
                iterator.remove(); 
            }
        }

        Iterator<Long> iteratorI = this.checkpointsI.keySet().iterator();
        while (iteratorI.hasNext()) {
            Long seqNumber = iteratorI.next();
            if (seqNumber < checkpoint) {
                iteratorI.remove();
            }
        }

        Iterator<Long> iteratorII = this.checkpointsII.keySet().iterator();
        while (iteratorII.hasNext()) {
            Long seqNumber = iteratorII.next();
            if (seqNumber < checkpoint) {
                iteratorII.remove();
            }
        }

        Iterator<Long> iteratorIII = this.checkpointsIII.keySet().iterator();
        while (iteratorIII.hasNext()) {
            Long seqNumber = iteratorIII.next();
            if (seqNumber < checkpoint) {
                iteratorIII.remove();
            }
        }

        // Reset the certificates
        // TODO: I am not sure whether these should be cleared or not
        // this.cer1 = new TreeMap<>();
        // this.cer2 = new TreeMap<>();
        // this.lastAcceptedCheckpointI = null;

        // TODO: probably fine to clear
        this.viewChanges.clear();

        this.lastStableCheckpoint = new Checkpoint(checkpoint, history);

        this.highWaterMark = checkpoint + this.watermarkInterval;
        this.lowWaterMark = checkpoint;
    }

    // FIXME: ReplicaId is null 
    public void appendPanic(PanicMessage panic, String replicaId) {
        this.panics.put(replicaId, panic);
    }

    public void clearPanics() {
        this.panics.clear();
    }

    public boolean checkPanics(int tolerance) {
        return this.panics.size() >= tolerance * 2 + 1;
    }

    public boolean checkPanicsForTimeout(int tolerance) {
        return this.panics.size() >= tolerance + 1;
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
            SortedMap<String, CheckpointMessage> checkpointProofs = this.checkpointsI.computeIfAbsent(seqNumber, k -> new TreeMap<>());
            checkpointProofs.put(checkpoint.getReplicaId(), checkpoint);
            acceptedCheckpointIs.put(viewNumber, new Checkpoint(seqNumber, history));
        }

        if (checkpoint instanceof CheckpointIIMessage) {
            SortedMap<String, CheckpointMessage> checkpointProofs = this.checkpointsII.computeIfAbsent(seqNumber, k -> new TreeMap<>());
            checkpointProofs.put(checkpoint.getReplicaId(), checkpoint);

            // Can produce duplicates
            Collection<SpeculativeHistory> cer1History = this.cer1.computeIfAbsent(viewNumber, k -> new ConcurrentLinkedQueue<>());
            cer1History.add(history);
        }

        if (checkpoint instanceof CheckpointIIIMessage) {
            SortedMap<String, CheckpointMessage> checkpointProofs = this.checkpointsIII.computeIfAbsent(seqNumber, k -> new TreeMap<>());
            checkpointProofs.put(checkpoint.getReplicaId(), checkpoint);

            // Can produce duplicates
            Collection<SpeculativeHistory> cer2History = this.cer2.computeIfAbsent(viewNumber, k -> new ConcurrentLinkedQueue<>());
            cer2History.add(history);

            final int stableCount = 2 * tolerance + 1;
            int matching = 0;

            for (CheckpointMessage proof : checkpointProofs.values()) {
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

    public boolean isCER1(CheckpointMessage checkpoint, int tolerance) {
        long seqNumber = checkpoint.getLastSeqNumber();
        // Should there be at least one because we add the checkpoint before calling this function
        SortedMap<String, CheckpointMessage> checkpointProofs = this.checkpointsII.computeIfAbsent(seqNumber, k -> new TreeMap<>());
        
        final int stableCount = 2 * tolerance + 1;
        int matching = 0;

        for (CheckpointMessage proof : checkpointProofs.values()) {
            if (Arrays.equals(proof.getDigest(), checkpoint.getDigest())) {
                matching++;
            }
        }

        return matching == stableCount;
    }

    public boolean isCER2(CheckpointMessage checkpoint, int tolerance) {
        long seqNumber = checkpoint.getLastSeqNumber();
        // Should there be at least one because we add the checkpoint before calling this function
        SortedMap<String, CheckpointMessage> checkpointProofs = this.checkpointsIII.computeIfAbsent(seqNumber, k -> new TreeMap<>());
        
        final int stableCount = 2 * tolerance + 1;
        int matching = 0;

        for (CheckpointMessage proof : checkpointProofs.values()) {
            if (Arrays.equals(proof.getDigest(), checkpoint.getDigest())) {
                matching++;
            }
        }

        return matching == stableCount;
    }

    public SpeculativeHistory isCER1inView(long viewNumber, int tolerance) {
        if (this.isNullOrEmpty(this.cer1.get(viewNumber))) {
            return null;
        } else {
            if (this.cer1.get(viewNumber).size() >= 2 * tolerance + 1) {
                return this.cer1.get(viewNumber).iterator().next();
            } 
            return null;
        }
    }

    /*
     * Checks whether a collection is empty
     */
    public boolean isNullOrEmpty( final Collection< ? > c ) {
        return c == null || c.isEmpty();
    }

    public ViewChangeMessage produceViewChange(long newViewNumber, long currViewNumber, String replicaId, int tolerance, SortedMap<Long,RequestMessage> speculativeRequests) {
        /*
         * Produces a VIEW-CHANGE vote message in accordance with hBFT 4.3.
         *
         * The last stable checkpoint is defined as the low water mark for the
         * message log.
         */
        long checkpoint = this.lowWaterMark;

        /* 
         * Speculatively executed requests with sequence number higher,
         * than the last accepted checkpoint (lowWaterMark)
         */
        SortedMap<Long, RequestMessage> requestsR = new TreeMap<>();
        for (long seqNumber : speculativeRequests.keySet()) {
            if (seqNumber > checkpoint) {
                requestsR.put(seqNumber, speculativeRequests.get(seqNumber));
            }
        }

        /* 
         * Execution history from previous view
         * CER1(M, v-1)
         */
        SpeculativeHistory historyP = this.isCER1inView(currViewNumber, tolerance);

        /* 
         * Q execution history from the accepted Checkpoint-I message
         * TODO: figure out which view is this
         */
        Checkpoint historyQ;
        if (this.acceptedCheckpointIs.get(currViewNumber) != null) {
            historyQ = this.acceptedCheckpointIs.get(currViewNumber);
        } else {
            historyQ = null;
        }

        // cer1 could be empty
        ViewChangeMessage viewChange = new ViewChangeMessage(
            newViewNumber,
            historyP,
            historyQ,
            requestsR,
            replicaId);

        SortedMap<String, ViewChangeMessage> newViewSet = this.viewChanges.computeIfAbsent(newViewNumber, k -> new TreeMap<>());
        newViewSet.put(replicaId, viewChange);

        return viewChange;
    }

    public ViewChangeResult acceptViewChange(ViewChangeMessage viewChange, String curReplicaId, long curViewNumber, int tolerance) {
        /*
         * Checks whether the view change is correct, 
         * the view is higher than the current
         * 
         * If f + 1 view-changes are received for the same view number
         * the replica accepts and also sends a view-change
         * (if it didnt send previously)
         * 
         * If gets 2f + 1 view-changes and this is the potential primary,
         * then sends a new-view (handled by replica itself)
         */
        long newViewNumber = viewChange.getNewViewNumber();
        String replicaId = viewChange.getReplicaId();

        SortedMap<String, ViewChangeMessage> newViewSet = this.viewChanges.computeIfAbsent(newViewNumber, k -> new TreeMap<>());

        // If the replica already sent a view-change for the same view,
        // we disregard it
        // TODO: might cause problems when a view change doesnt happen
        // if (newViewSet.containsKey(replicaId)) {
        //     return new ViewChangeResult(false, newViewNumber, false);
        // }

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

        final int newViewThreshold = 2 * tolerance;
        boolean newView = totalVotes == newViewThreshold;

        return new ViewChangeResult(shouldBandwagon, smallestView, newView);
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

        Map<Long, Integer> checkpointMap = new HashMap<>();

        // Replica needs all the possibly executed requests for later
        Map<ClientRequestKey, Integer> requestMap = new HashMap<>();
        Collection<SortedMap<Long, RequestMessage>> allRequests = new ArrayList<>();

        // Rule A1: Check if speculative history M has CER1(M, v) from at least 2f + 1 replicas
        // Loop through the view-change messages and try to find 2f + 1 matching P history
        for (ViewChangeMessage viewChangePerReplica : newViewSet.values()) {
            SpeculativeHistory pHistory = viewChangePerReplica.getSpeculativeHistoryP();
            Checkpoint qHistory = viewChangePerReplica.getSpeculativeHistoryQ();
            SortedMap<Long, RequestMessage> requests = viewChangePerReplica.getRequestsR();
            allRequests.add(requests);

            for (long seqNum : requests.keySet()) {
                ClientRequestKey key = new ClientRequestKey(seqNum, requests.get(seqNum).getOperation().toString());
                requestMap.put(key, requestMap.getOrDefault(key, 0) + 1);
            }
            
            // We tackle the empty history later in rule B
            if (pHistory == null || pHistory.isEmpty()) {
                continue;
            }

            historyMap.put(pHistory, historyMap.getOrDefault(pHistory, 0) + 1);
            if (qHistory != null) {
                checkpointMap.put(qHistory.getSequenceNumber(), checkpointMap.getOrDefault(qHistory.getSequenceNumber(), 0) + 1);
            }
        }

        for (Map.Entry<SpeculativeHistory, Integer> entry : historyMap.entrySet()) {
            if (entry.getValue() >= certificateTolerance) {
                selectedHistoryM = entry.getKey();
            }
        }

        // Rule A2: At least f + 1 replicas accepted Checkpoint-I in view v' > v
        if (selectedHistoryM != null) {
            final int stableCount = tolerance + 1;

            for (Long key : checkpointMap.keySet()) {
                if (checkpointMap.get(key) >= stableCount) {
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
                ClientRequestKey key = new ClientRequestKey(request.getKey(), request.getValue().getOperation().toString());
                //System.out.println(selectedHistoryM + " " + request + " " + (requestMap.get(key) >= tolerance + 1));
                if ((selectedHistoryM == null || request.getKey() > selectedHistoryM.getGreatestSeqNumber()) && requestMap.get(key) >= tolerance + 1) {
                    sortedRequests.addEntry(request.getKey(), request.getValue());
                } else if (selectedHistoryM == null || request.getKey() > selectedHistoryM.getGreatestSeqNumber()) {
                    if (!sortedRequests.getRequests().containsKey(request.getKey())) {                    
                        sortedRequests.addEntry(request.getKey(), null);
                    }
                }
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


        // TODO: Might need a fix
        if (selectedCheckpoint != null && selectedCheckpoint.getHistory() != null) {
            SortedMap<Long, RequestMessage> historyMRequests = selectedCheckpoint.getHistory().getRequests();
            for (long seqNumber : selectedCheckpoint.getHistory().getRequests().keySet()) {
                sortedRequests.addEntry(seqNumber, historyMRequests.get(seqNumber));
            }
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

        Iterator<TicketKey> iterator = this.tickets.keySet().iterator();
        while (iterator.hasNext()) {
            TicketKey key = iterator.next();
            if (key.getViewNumber() != newViewNumber) {
                iterator.remove();
            }
        }
    }

    public boolean acceptNewView(NewViewMessage newView, long tolerance, ScheduleLogger logger) {
        /*
         * When receiving a new view message the replicas will 
         * run a checkpoint sub-protocol (started by the primary)
         */

        // TODO: verify view-change
        Collection<ViewChangeMessage> viewChanges = newView.getViewChangeProofs();
        Checkpoint checkpoint = newView.getCheckpoint();
        SpeculativeHistory requestsFromNewView = newView.getSpeculativeHistory();

        // Should never be null
        if (checkpoint == null) {
            logger.writeLog("Checkpoint is null!");
            return false;
        }
        // If any of the view-change messages are incorrect then dont accept
        for (ViewChangeMessage viewChangeMessage : viewChanges) {
            if (viewChangeMessage.getNewViewNumber() != newView.getNewViewNumber()) {
                logger.writeLog("Includes incorrect view change!");
                return false;
            }
        }

        // If the checkpoint is different than what should have been
        // based on the view-change messages then dont accept
        long threshold = tolerance * 2 + 1;
        long count = 0;
        ViewChangeMessage firstViewChange = viewChanges.iterator().next();
        for (ViewChangeMessage viewChangeMessage : viewChanges) {
            if (viewChangeMessage.getSpeculativeHistoryP() != null 
                && (viewChangeMessage.getSpeculativeHistoryP().getGreatestSeqNumber() == checkpoint.getSequenceNumber()
                || viewChangeMessage.getSpeculativeHistoryP().equals(checkpoint.getHistory()))) {
                count++;
            }
        }

        if (count >= threshold
            && firstViewChange.getSpeculativeHistoryP() != null
            && (firstViewChange.getSpeculativeHistoryP().getGreatestSeqNumber() != checkpoint.getSequenceNumber()
            || !firstViewChange.getSpeculativeHistoryP().equals(checkpoint.getHistory()))) {
                //System.out.println("Checkpoint should be matching! (SpeculativeHistoryP)");
                logger.writeLog("Checkpoint should be matching! (SpeculativeHistoryP)");
                return false;
            }

        Map<Checkpoint, Integer> historyQmap = new TreeMap<>();
        long thresholdForCheckpointI = tolerance + 1;

        for (ViewChangeMessage viewChangeMessage : viewChanges) {
            if (viewChangeMessage.getSpeculativeHistoryQ() == null) {
                continue;
            }
            int prevVal = historyQmap.getOrDefault(viewChangeMessage.getSpeculativeHistoryQ(), 0);
            historyQmap.put(viewChangeMessage.getSpeculativeHistoryQ(), prevVal + 1);
        }

        for (Checkpoint check : historyQmap.keySet()) {
            if (historyQmap.get(check) >= thresholdForCheckpointI) {
                if (check.getSequenceNumber() != checkpoint.getSequenceNumber()
                    || !check.getHistory().equals(checkpoint.getHistory())) {
                    //System.out.println("Checkpoint-I should be matching! (SpeculativeHistoryQ)");
                    logger.writeLog("Checkpoint-I should be matching! (SpeculativeHistoryQ)");
                    return false;
                }
            }
        }

        /* 
        * If the checkpoint is not chosen from the view-change messages then,
        * the new primary must pick its own last stable checkpoint,
        * which should match for 2f + 1 replicas, otherwise the operation
        * cannot continue. Even if the new primary is correct, this replica 
        * can reject this new-view, however 2f + 1 replicas should accept, 
        * and continue operating. This replica can rejoin at a checkpoint 
        * sub-protocol.
        */
        //FIXME: If replicas accept a checkpoint, their checkpoint changes, not working properly currently or might be a bug
        // if ((countForCheckpiontI < thresholdForCheckpointI || count < threshold) && !checkpoint.equals(this.lastStableCheckpoint)) {
        //     logger.writeLog("Choosing primary's checkpoint and its incorrect!");
        //     return false;
        // }

        // Replica needs all the possibly executed requests for later
        Map<ClientRequestKey, Integer> requestMap = new HashMap<>();
        Collection<SortedMap<Long, RequestMessage>> allRequests = new ArrayList<>();

        for (ViewChangeMessage viewChangePerReplica : viewChanges) {
            SortedMap<Long, RequestMessage> requestsPerReplica = viewChangePerReplica.getRequestsR();
            allRequests.add(requestsPerReplica);

            for (long seqNum : requestsPerReplica.keySet()) {
                ClientRequestKey key = new ClientRequestKey(seqNum, requestsPerReplica.get(seqNum).getOperation().toString());
                requestMap.put(key, requestMap.getOrDefault(key, 0) + 1);
            }
        }

        SpeculativeHistory sortedRequests = new SpeculativeHistory();
        // System.out.println(requestMap + " " + allRequests);

        /*  
         * Select every request in R if f+1 replicas include it
         * And sequence number is greater than the largest in selectedHistoryM
         */
        for (SortedMap<Long, RequestMessage> requests : allRequests) {
            for (Map.Entry<Long, RequestMessage> request : requests.entrySet()) {
                ClientRequestKey key = new ClientRequestKey(request.getKey(), request.getValue().getOperation().toString());
                if ((checkpoint == null || request.getKey() > checkpoint.getSequenceNumber()) && requestMap.get(key) >= tolerance + 1) {
                    sortedRequests.addEntry(request.getKey(), request.getValue());
                } else if (checkpoint == null || request.getKey() > checkpoint.getSequenceNumber()) {
                    if (!sortedRequests.getRequests().containsKey(request.getKey())) {                    
                        sortedRequests.addEntry(request.getKey(), null);
                    }
                }
            }
        }

        if (checkpoint != null && checkpoint.getHistory() != null) {
            SortedMap<Long, RequestMessage> historyMRequests = checkpoint.getHistory().getRequests();
            for (long seqNumber : checkpoint.getHistory().getRequests().keySet()) {
                sortedRequests.addEntry(seqNumber, historyMRequests.get(seqNumber));
            }
        }

        //System.out.println("Locally sorted requests: " + sortedRequests + "\n Received: " + requestsFromNewView);

        if (!sortedRequests.getRequests().keySet().equals(requestsFromNewView.getRequests().keySet())) {
            logger.writeLog("Requests received and sorted do not match!");
            return false;
        }

        for (long seqNumber : sortedRequests.getRequests().keySet()) {
            if (sortedRequests.getRequests().get(seqNumber) != requestsFromNewView.getRequests().get(seqNumber)
                || (sortedRequests.getRequests().get(seqNumber) == null && requestsFromNewView.getRequests().get(seqNumber) != null)
                || (sortedRequests.getRequests().get(seqNumber) != null && requestsFromNewView.getRequests().get(seqNumber) == null)) {
                logger.writeLog("Requests received and sorted do not match!");
                return false;
            }
        }

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

    public <O> void bufferFirst(RequestMessage request) {
        this.buffer.addFirst(request);
    }

    public <O> RequestMessage popBuffer() {
        return this.buffer.pollFirst();
    }

    public boolean isBetweenWaterMarks(long seqNumber) {
        return seqNumber >= this.lowWaterMark && seqNumber <= this.highWaterMark;
    }
}
