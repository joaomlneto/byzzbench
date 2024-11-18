package byzzbench.simulator.protocols.fab.replicas;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.protocols.pbft_java.MessageLog;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.SortedSet;

/**
 * A replica in the FAB protocol.
 */
@Log
@Getter
public abstract class FabReplica extends LeaderBasedProtocolReplica {

    /**
     * The log of received messages for the replica.
     */
    @JsonIgnore
    private final MessageLog messageLog;

    public FabReplica(String nodeId, SortedSet<String> nodeIds, Transport transport, MessageLog messageLog) {
        super(nodeId, nodeIds, transport, new TotalOrderCommitLog());
        this.messageLog = messageLog;
    }

    @Override
    public void initialize() {
        System.out.println("Initializing FAB replica " + getNodeId());
    }

    @Override
    public void handleClientRequest(String clientId, Serializable request) throws Exception {

    }

    @Override
    public void handleMessage(String sender, MessagePayload message) throws Exception {

    }
}
