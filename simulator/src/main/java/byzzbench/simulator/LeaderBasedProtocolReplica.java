package byzzbench.simulator;

import byzzbench.simulator.state.CommitLog;
import byzzbench.simulator.transport.Transport;
import lombok.Getter;

import java.util.Set;

/**
 * Abstract class for a replica that is part of a leader-based protocol.
 */
@Getter
public abstract class LeaderBasedProtocolReplica extends Replica {
    private long viewNumber = -1;
    private String leaderId;

    protected LeaderBasedProtocolReplica(String nodeId, Set<String> nodeIds, Transport transport, CommitLog commitLog) {
        super(nodeId, nodeIds, transport, commitLog);
    }

    /**
     * Get the current view of the replica.
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
}
