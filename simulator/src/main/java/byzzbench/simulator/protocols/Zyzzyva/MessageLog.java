package byzzbench.simulator.protocols.Zyzzyva;

import byzzbench.simulator.protocols.Zyzzyva.message.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.Serializable;
import java.util.*;

// Should hold the Ordered request message wrappers received by the replica
@Getter
@Log
public class MessageLog implements Serializable {
    /// TODO: Add checkpoints as well as history? Maybe digest the history

    // SequenceNumber, OrderedRequestMessageWrapper
    private final SortedMap<Long, OrderedRequestMessageWrapper> orderedMessages = new TreeMap<>();

    // SequenceNumber, Map(ReplicaId -> SpeculativeResponse)
    private final SortedMap<Long, SortedMap<String, SpeculativeResponse>> speculativeResponsesCheckpoint = new TreeMap<>();

    // ViewNumber, Map(ReplicaId -> IHateThePrimaryMessage)
    private final SortedMap<Long, SortedMap<String, IHateThePrimaryMessage>> iHateThePrimaries = new TreeMap<>();

    // SequenceNumber, Map(ReplicaId -> FillHoleMessage) received
    private final SortedMap<Long, SortedMap<String, FillHoleReply>> fillHoleMessages = new TreeMap<>();

    // ViewNumber, Map(ReplicaId -> ViewChangeMessage)
    private final SortedMap<Long, SortedMap<String, ViewConfirmMessage>> viewConfirmMessages = new TreeMap<>();

    // ViewNumber, ViewChangeMessage
    private final SortedMap<Long, SortedMap<String, ViewChangeMessage>> viewChangeMessages = new TreeMap<>();

    // ViewNumber, NewViewMessage
    private final SortedMap<Long, NewViewMessage> newViewMessages = new TreeMap<>();

    // ClientId, -> (Timestamp, SpeculativeResponseWrapper)
    private final SortedMap<String, ImmutablePair<RequestMessage, SpeculativeResponseWrapper>> responseCache = new TreeMap<>();

    private final SortedMap<Long, CommitCertificate> commitCertificates = new TreeMap<>();

    // ClientId, RequestMessage
    // Used to store the latest request from a client for the forward to primary
    private final SortedMap<String, RequestMessage> requestCache = new TreeMap<>();
    // SequenceNumber, Map(ReplicaId -> CheckpointMessage)
    private final SortedMap<Long, SortedMap<String, CheckpointMessage>> checkpointMessages = new TreeMap<>();
    // SequenceNumber, CommitMessage
    // used in an edge case where we receive a commit message during a fill hole
    private final SortedMap<Long, CommitMessage> commitMessages = new TreeMap<>();

    @Setter
    // view number of the last POM
    private long lastPOM;

    @Setter
    private long lastCheckpoint;

    private CommitCertificate maxCC;

    public MessageLog() {
        // create a null checkpoint message
        this.setLastCheckpoint(0L);
        CheckpointMessage cmA = new CheckpointMessage(0L, 0L, "A");
        cmA.sign("A");
        CheckpointMessage cmB = new CheckpointMessage(0L, 0L, "B");
        cmB.sign("B");
        CheckpointMessage cmC = new CheckpointMessage(0L, 0L, "C");
        cmC.sign("C");
        CheckpointMessage cmD = new CheckpointMessage(0L, 0L, "D");
        cmD.sign("D");
        this.putCheckpointMessage(cmA);
        this.putCheckpointMessage(cmB);
        this.putCheckpointMessage(cmC);
        this.putCheckpointMessage(cmD);
    }

    /**
     * Get the ordered request history from the sequence number to the end of the ordered messages
     * @param sequenceNumber - the sequence number to start the history from
     * @return - a map of the ordered request history with the sequence number as the key
     */
    public SortedMap<Long, OrderedRequestMessageWrapper> getOrderedRequestHistory(long sequenceNumber) {
        SortedMap<Long, OrderedRequestMessageWrapper> orderedRequestHistory = new TreeMap<>();
        for (long i : this.getOrderedMessages().sequencedKeySet()) {
            if (i < sequenceNumber) {
                continue;
            }
            orderedRequestHistory.put(i, this.orderedMessages.get(i));
        }
        return orderedRequestHistory;
    }

    public void setMaxCC(CommitCertificate cc) {
        this.getCommitCertificates().put(cc.getSequenceNumber(), cc);
        this.maxCC = cc;
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
        this.getViewConfirmMessages().putIfAbsent(vcm.getFutureViewNumber(), new TreeMap<>());
        this.getViewConfirmMessages().get(vcm.getFutureViewNumber()).put(vcm.getSignedBy(), vcm);
    }

    public void putNewViewMessage(NewViewMessage nvm) {
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

    public void rollbackOrderedRequestMessages(long sequenceNumber) {
        if (this.getOrderedMessages().isEmpty()) return;
        Set<Long> keys = new HashSet<>(this.getOrderedMessages().keySet());

        for (long seqNum : keys) {
            if (seqNum > sequenceNumber) {
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

    public void putResponseCache(String clientId, RequestMessage rm, SpeculativeResponseWrapper srw) {
        if (this.highestTimestampInCacheForClient(clientId) > rm.getTimestamp()) {
            if (clientId.equals("Noop")) {
                return;
            }
            /// TODO: silence this during a view change, it doesn't mean anything. You can do this by comparing the view number to the request cache one.
            log.warning("Received a request with a timestamp smaller than the one in the cache");
            return;
        }
        this.getResponseCache().put(clientId, new ImmutablePair<>(rm, srw));
    }

    public void putRequestCache(String clientId, RequestMessage rm) {
        this.getRequestCache().put(clientId, rm);
    }

    public void putOrderedRequestMessageWrapper(OrderedRequestMessageWrapper ormw) {
        this.getOrderedMessages().put(ormw.getOrderedRequest().getSequenceNumber(), ormw);
    }

    public long highestTimestampInCacheForClient(String clientId) {
        if (!this.getResponseCache().containsKey(clientId)) {
            return -1;
        }
        return this.getResponseCache().get(clientId).getLeft().getTimestamp();
    }

    public void putFillHoleMessage(FillHoleReply fhm) {
        this.getFillHoleMessages().putIfAbsent(fhm.getOrderedRequestMessage().getSequenceNumber(), new TreeMap<>());
        this.getFillHoleMessages().get(fhm.getOrderedRequestMessage().getSequenceNumber()).put(fhm.getSignedBy(), fhm);
    }

    public void setLastRequest(String clientId, RequestMessage rm) {
        this.getRequestCache().put(clientId, rm);
    }

    public void putCommitMessage(CommitMessage commitMessage) {
        this.getCommitMessages().put(commitMessage.getCommitCertificate().getSequenceNumber(), commitMessage);
    }
}