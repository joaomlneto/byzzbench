package byzzbench.simulator.protocols.Zyzzyva;

import byzzbench.simulator.protocols.Zyzzyva.message.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.Serializable;
import java.util.*;

// Should hold the Ordered request message wrappers received by the replica
@Log
public class MessageLog implements Serializable {
    /// TODO: Add checkpoints as well as history? Maybe digest the history

    @Getter
    // SequenceNumber, OrderedRequestMessageWrapper
    private final SortedMap<Long, OrderedRequestMessageWrapper> orderedMessages = new TreeMap<>();

    @Getter
    // SequenceNumber, SpeculativeResponse
    private final SortedMap<Long, SpeculativeResponse> speculativeResponses = new TreeMap<>();

    @Getter
    // SequenceNumber, Map(ReplicaId -> SpeculativeResponse)
    private final SortedMap<Long, SortedMap<String, SpeculativeResponse>> speculativeResponsesCheckpoint = new TreeMap<>();

    @Getter
    // ViewNumber, Map(ReplicaId -> IHateThePrimaryMessage)
    private final SortedMap<Long, SortedMap<String, IHateThePrimaryMessage>> iHateThePrimaries = new TreeMap<>();

    @Getter
    // SequenceNumber, Map(ReplicaId -> FillHoleMessage) received
    private final SortedMap<Long, SortedMap<String, FillHoleReply>> fillHoleMessages = new TreeMap<>();

    @Getter
    // ViewNumber, ViewConfirmMessage
    private final SortedMap<Long, List<ViewConfirmMessage>> viewConfirmMessages = new TreeMap<>();

    @Getter
    // ViewNumber, ViewChangeMessage
    private final SortedMap<Long, SortedMap<String, ViewChangeMessage>> viewChangeMessages = new TreeMap<>();

    @Getter
    // ViewNumber, NewViewMessage
    private final SortedMap<Long, NewViewMessage> newViewMessages = new TreeMap<>();

    @Getter
    // ClientId, -> (Timestamp, SpeculativeResponseWrapper)
    private final SortedMap<String, ImmutablePair<RequestMessage, SpeculativeResponseWrapper>> responseCache = new TreeMap<>();

    @Getter
    // SequenceNumber, Map(ReplicaId -> CheckpointMessage)
    private final SortedMap<Long, SortedMap<String, CheckpointMessage>> checkpointMessages = new TreeMap<>();

    @Getter
    @Setter
    private Long lastCheckpoint;

    @Getter
    @Setter
    private CommitCertificate maxCC;

    public MessageLog() {
        // create a null checkpoint message
        this.setLastCheckpoint(0L);
    }

    public List<OrderedRequestMessageWrapper> getOrderedRequestHistory() {
        long maxCCSeqNum = this.maxCC.getSequenceNumber();
        List<OrderedRequestMessageWrapper> orderedRequestHistory = new ArrayList<>();
        long maxSeqNum = this.getOrderedMessages().isEmpty() ? 0 : this.getOrderedMessages().lastKey();
        for (long i = maxCCSeqNum + 1; i <= maxSeqNum; i++) {
            orderedRequestHistory.add(this.orderedMessages.get(i));
        }
        return orderedRequestHistory;
    }

    public List<SpeculativeResponse> getSpeculativeResponseHistory(long index) {
        long maxSeqNum = this.getSpeculativeResponses().pollLastEntry().getValue().getSequenceNumber();
        List<SpeculativeResponse> speculativeResponseHistory = new ArrayList<>();
        for (long i = index + 1; i <= maxSeqNum; i++) {
            speculativeResponseHistory.add(this.speculativeResponses.get(i));
        }
        return speculativeResponseHistory;
    }

    public void putIHateThePrimaryMessage(IHateThePrimaryMessage ihtpm) {
        this.getIHateThePrimaries().putIfAbsent(ihtpm.getViewNumber(), new TreeMap<>());
        this.getIHateThePrimaries().get(ihtpm.getViewNumber()).put(ihtpm.getSignedBy(), ihtpm);
    }

    public void putViewChangeMessage(ViewChangeMessage vcm) {
        this.getViewChangeMessages().putIfAbsent(vcm.getFutureViewNumber(), new TreeMap<>());
        this.getViewChangeMessages().get(vcm.getFutureViewNumber()).put(vcm.getSignedBy(), vcm);
    }

    public void putViewConfirmMessage(ViewConfirmMessage vcm) {
        this.getViewConfirmMessages().putIfAbsent(vcm.getFutureViewNumber(), new ArrayList<>());
        this.getViewConfirmMessages().get(vcm.getFutureViewNumber()).add(vcm);
    }

    public void putNewViewMessage(NewViewMessage nvm) {
        /// TODO: check if it's sanitized? as in the new view number is sent by the appropriate replica
        this.getNewViewMessages().put(nvm.getFutureViewNumber(), nvm);
    }

    public void putCheckpointMessage(CheckpointMessage cm) {
        this.getCheckpointMessages().putIfAbsent(cm.getSequenceNumber(), new TreeMap<>());
        this.getCheckpointMessages().get(cm.getSequenceNumber()).put(cm.getReplicaId(), cm);
    }

    public void putSpeculativeResponseCheckpoint(long sequenceNumber, SpeculativeResponse speculativeResponse) {
        this.getSpeculativeResponsesCheckpoint().putIfAbsent(sequenceNumber, new TreeMap<>());
        this.getSpeculativeResponsesCheckpoint().get(sequenceNumber).put(speculativeResponse.getSignedBy(), speculativeResponse);
    }

    /**
     * Truncate the checkpoint message history
     * @param sequenceNumber - the sequence number to truncate the history to (non-inclusive)
     */
    public void truncateCheckpointMessages(long sequenceNumber) {
        if (this.getCheckpointMessages().isEmpty()) {
            return;
        }
        Set<Long> keys = new HashSet<>(this.getCheckpointMessages().keySet());
        for (long seqNum : keys) {
            if (seqNum < sequenceNumber) {
                this.getCheckpointMessages().remove(seqNum);
            }
        }
    }

    /**
     * Truncate the speculative response history
     * Used in garbage collection after checkpointing
     * @param sequenceNumber - the sequence number to truncate the history to (non-inclusive)
     */
    public void truncateSpeculativeResponseMessages(long sequenceNumber) {
        if (this.getSpeculativeResponses().isEmpty()) return;
        Set<Long> keys = new HashSet<>(this.getSpeculativeResponses().keySet());

        for (long seqNum : keys) {
            if (seqNum < sequenceNumber) {
                this.getSpeculativeResponses().remove(seqNum);
            }
        }
    }

    /**
     * Truncate the ordered request history
     * @param sequenceNumber - the sequence number to truncate the history to (non-inclusive)
     */
    public void truncateOrderedRequestMessages(long sequenceNumber) {
        if (this.getOrderedMessages().isEmpty()) return;
        Set<Long> keys = new HashSet<>(this.getOrderedMessages().keySet());

        for (long seqNum : keys) {
            if (seqNum < sequenceNumber) {
                this.getOrderedMessages().remove(seqNum);
            }
        }
    }

    /**
     * Truncate the fill hole message history
     * @param sequenceNumber - the sequence number to truncate the history to (non-inclusive)
     */
    public void truncateFillHoleMessages(long sequenceNumber) {
        if (this.getFillHoleMessages().isEmpty()) return;
        Set<Long> keys = new HashSet<>(this.getFillHoleMessages().keySet());

        for (long seqNum : keys) {
            if (seqNum < sequenceNumber) {
                this.getFillHoleMessages().remove(seqNum);
            }
        }
    }

    public void putRequestCache(String clientId, RequestMessage rm, SpeculativeResponseWrapper srw) {
        if (this.highestTimestampInCacheForClient(clientId) > rm.getTimestamp()) {
            log.warning("Received a request with a timestamp smaller than the one in the cache");
            return;
        }
        this.getResponseCache().put(clientId, new ImmutablePair<>(rm, srw));
    }

    public long highestTimestampInCacheForClient(String clientId) {
        if (!this.getResponseCache().containsKey(clientId)) {
            return -1;
        }
        return this.getResponseCache().get(clientId).getLeft().getTimestamp();
    }

}