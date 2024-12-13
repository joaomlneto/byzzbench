package byzzbench.simulator.protocols.Zyzzyva;

import byzzbench.simulator.protocols.Zyzzyva.message.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

// Should hold the Ordered request message wrappers received by the replica
public class MessageLog implements Serializable {
    /// TODO: Add checkpoints as well as history? Maybe digest the history
    private final int checkpointInterval;

    @Getter
    // SequenceNumber, OrderedRequestMessageWrapper
    private final SortedMap<Long, OrderedRequestMessageWrapper> orderedMessages = new TreeMap<>();

    @Getter
    private final SortedMap<Long, SpeculativeResponse> speculativeResponses = new TreeMap<>();

    @Getter
    // ReplicaId, IHateThePrimaryMessage
    private final SortedMap<String, IHateThePrimaryMessage> iHateThePrimaries = new TreeMap<>();

    @Getter
    // SequenceNumber, Map(ReplicaId -> FillHoleMessage) received
    private final SortedMap<Long, SortedMap<String, FillHoleReply>> fillHoleMessages = new TreeMap<>();

    @Getter
    // ViewNumber, ViewConfirmMessage
    private final SortedMap<Long, List<ViewConfirmMessage>> viewConfirmMessages = new TreeMap<>();

    @Getter
    // viewNumber, ViewChangeMessage
    private final SortedMap<Long, SortedMap<String, ViewChangeMessage>> viewChangeMessages = new TreeMap<>();

    @Getter
    // viewNumber, NewViewMessage
    private final SortedMap<Long, NewViewMessage> newViewMessages = new TreeMap<>();

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
        long maxSeqNum = this.orderedMessages.pollLastEntry().getValue().getOrderedRequest().getSequenceNumber();
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
}