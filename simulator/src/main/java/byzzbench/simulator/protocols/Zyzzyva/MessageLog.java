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
    private final int checkpointInterval;

    @Getter
    // SequenceNumber, OrderedRequestMessageWrapper
    private final SortedMap<Long, OrderedRequestMessageWrapper> orderedMessages = new TreeMap<>();

    @Getter
    // SequenceNumber, SpeculativeResponse
    private final SortedMap<Long, SpeculativeResponse> speculativeResponses = new TreeMap<>();

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
    private final long lastCheckpoint = 0;

    @Getter
    @Setter
    private CommitCertificate maxCC;

    public MessageLog(int checkpointInterval) {
        this.checkpointInterval = checkpointInterval;
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

    public void truncateSpeculativeResponseHistory() {
        if (this.getSpeculativeResponses().isEmpty()) {
            return;
        }
        long maxSeqNum = this.getSpeculativeResponses().pollLastEntry().getValue().getSequenceNumber();
        for (long i = 1; i <= maxSeqNum; i++) {
            this.speculativeResponses.remove(i);
        }
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
        this.getNewViewMessages().put(nvm.getFutureViewNumber(), nvm);
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