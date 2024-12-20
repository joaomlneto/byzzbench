package byzzbench.simulator.protocols.event_driven_hotstuff;

import byzzbench.simulator.protocols.event_driven_hotstuff.messages.NewViewMessage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

public class EDHSPacemaker {
    @JsonIgnore
    private final transient EDHotStuffReplica replica;
    @Getter
    EDHSQuorumCertificate highQC;
    @Getter
    EDHSNode leafNode;

    // For debugging only
    public Long getHighQCHeight() {
        EDHSNode node = replica.getNode(highQC.getNodeHash());
        if (node == null) return Long.MIN_VALUE;
        return node.getHeight();
    }

    public EDHSPacemaker(EDHotStuffReplica replica, EDHSNode initialLeft, EDHSQuorumCertificate initialHighQC) {
        this.replica = replica;
        this.highQC = initialHighQC;
        this.leafNode = initialLeft;
    }

    public String getLeaderId(long viewNumber) {
        int leaderIndex = (int) (viewNumber % (replica.getReplicaIds().size()));

        return replica.getReplicaIds().get(leaderIndex);
    }

    public String getLeaderId() {
        return getLeaderId(replica.getViewNumber());
    }

    public boolean isLeader(long viewNumber) {
        String leaderId = getLeaderId(viewNumber);
        return replica.getId().equals(leaderId);
    }

    public boolean isLeader() {
        return isLeader(replica.getViewNumber());
    }

    public void updateHighQC(EDHSQuorumCertificate newQC) {
        if (newQC.equals(highQC)) return;

        EDHSNode newQCNode = replica.getNode(newQC.getNodeHash());
        EDHSNode highQCNode = replica.getNode(highQC.getNodeHash());

        if (newQCNode.getHeight() > highQCNode.getHeight()) {
            replica.log("Updating highQC with QC for node " + newQCNode.getHash() + " with height " + newQCNode.getHeight());
            highQC = newQC;
            leafNode = newQCNode;
        }
    }

    public void onNewViewQuorum() { onBeat(); }
    public void onNewQC() { onBeat(); }
    public void onClientRequest() { onBeat(); }

    private void onBeat() {
        try {
            LeaderViewState viewState = replica.getLeaderViewState();
            if (viewState == null) {
                replica.log("Cannot create proposal. Not leader for view " + replica.getViewNumber());
                return;
            }

            if(!viewState.isDone() && (viewState.hasMadeQC() || viewState.hasNewViewQuorum())) {
                ClientRequest clientRequest = replica.nextClientRequest(leafNode);
                if (clientRequest != null) {
                    //leafNode =
                    replica.onPropose(leafNode, clientRequest, highQC);
                    viewState.setViewActionDone();
                    replica.log("Created proposal with height " + leafNode.getHeight() + " for request " + clientRequest.getRequestId());
                } else replica.log("Cannot create proposal. No known client requests.");
            }
        } catch (Exception ignored) {}
    }

    public void onNextSyncView() {
        //clearAllTimeouts();

        replica.nextView();
        replica.sendMessage(new NewViewMessage(replica.getViewNumber(), highQC), getLeaderId());

        replica.log("Next view sync timeout");
        //setTimeout(this::onNextSyncView, 30000);
    }

    // NEW-VIEW
    public void onReceiveNewView(NewViewMessage newViewMessage) {
        updateHighQC((newViewMessage).getJustify());
    }
}
