package byzzbench.simulator.protocols.fab.replicas;

import byzzbench.simulator.protocols.pbft_java.MessageLog;
import byzzbench.simulator.transport.Transport;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.SortedSet;

@Log
@Getter
public class AcceptorReplica extends FabReplica{
    private final FabRole role;

    public AcceptorReplica(String nodeId, SortedSet<String> nodeIds, Transport transport, MessageLog messageLog) {
        super(nodeId, nodeIds, transport, messageLog);
        this.role = FabRole.ACCEPTOR;
    }
}
