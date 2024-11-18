package byzzbench.simulator.protocols.fab.replicas;

import byzzbench.simulator.protocols.pbft_java.MessageLog;
import byzzbench.simulator.transport.Transport;

import java.util.SortedSet;

public class LearnerReplica extends FabReplica {
    private final FabRole role;

    public LearnerReplica(String nodeId, SortedSet<String> nodeIds, Transport transport, MessageLog messageLog) {
        super(nodeId, nodeIds, transport, messageLog);
        this.role = FabRole.LEARNER;
    }
}
