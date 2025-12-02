package byzzbench.simulator.protocols.faulty_deadlock;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.nodes.ClientRequest;
import byzzbench.simulator.nodes.Replica;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import lombok.ToString;
import lombok.extern.java.Log;

@Log
@ToString(callSuper = true)
public class FaultyDeadlockReplica extends Replica {
    public FaultyDeadlockReplica(String replicaId, Scenario scenario) {
        super(replicaId, scenario, new TotalOrderCommitLog());
    }

    @Override
    public void initialize() {
        // nothing to do
    }

    @Override
    public void handleMessage(String sender, MessagePayload m) {
        if (m instanceof ClientRequest clientRequestMessage) {
            // do nothing... will cause deadlock
        } else {
            throw new UnsupportedOperationException("Unknown message type: " + m.getType());
        }
    }

}

