package byzzbench.simulator;

import byzzbench.simulator.state.CommitLog;
import byzzbench.simulator.transport.Transport;
import lombok.Getter;

import java.io.Serializable;
import java.util.Set;

/**
 * Abstract class for a replica that is part of a leader-based protocol.
 *
 * @param <T> The type of the entries in the commit log.
 */
@Getter
public abstract class LeaderBasedProtocolReplica<T extends Serializable> extends Replica<T> {
    private long viewNumber = -1;
    private String leaderId;

    protected LeaderBasedProtocolReplica(String nodeId, Set<String> nodeIds, Transport<T> transport, CommitLog<T> commitLog) {
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
