package byzzbench.simulator.nodes;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.state.CommitLog;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for a replica that is part of a leader-based protocol.
 */
@Getter
public abstract class LeaderBasedProtocolReplica extends Replica {
    private long viewNumber = -1;
    private String leaderId;

    protected LeaderBasedProtocolReplica(String nodeId, Scenario scenario, CommitLog commitLog) {
        super(nodeId, scenario, commitLog);
    }

    /**
     * Set the current view of the replica.
     *
     * @param viewNumber The view number.
     * @param leaderId   The ID of the leader.
     */
    public void setView(long viewNumber, String leaderId) {
        this.viewNumber = viewNumber;
        this.leaderId = leaderId;

        // notify the distributed state of the leader change
        this.notifyObserversLeaderChange(leaderId);
    }

    /**
     * Set the current view of the replica, and set the leader ID to the round-robin primary.
     *
     * @param viewNumber The view number.
     */
    public void setView(long viewNumber) {
        this.setView(viewNumber, this.getRoundRobinPrimaryId(viewNumber));
    }

    /**
     * Get the primary ID for the current view, round-robin style.
     *
     * @return The ID of the primary replica.
     */
    public String getRoundRobinPrimaryId() {
        if (this.viewNumber < 0) {
            throw new IllegalStateException("View number has not yet been set!");
        }

        return this.getRoundRobinPrimaryId(this.viewNumber);
    }

    /**
     * Get the primary ID for a given view, round-robin style.
     *
     * @param view The view number.
     * @return The ID of the primary replica.
     */
    public String getRoundRobinPrimaryId(long view) {
        if (view < 0) {
            throw new IllegalStateException("Invalid view number: " + view);
        }

        List<String> sortedNodeIds = new ArrayList<>(this.getScenario().getReplicaIds(this));
        int numNodes = sortedNodeIds.size();
        return sortedNodeIds.get((int) (view % numNodes));
    }

    /**
     * Send a message to the leader.
     *
     * @param message The message to send.
     */
    public void sendMessageToLeader(MessagePayload message) {
        this.sendMessage(message, this.getLeaderId());
    }

    /**
     * Check if the replica is the leader.
     *
     * @return True if the replica is the leader, false otherwise.
     */
    public boolean amILeader() {
        return this.getId().equals(this.getLeaderId());
    }
}
