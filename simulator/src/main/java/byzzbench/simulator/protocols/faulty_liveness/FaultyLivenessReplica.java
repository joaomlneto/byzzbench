package byzzbench.simulator.protocols.faulty_liveness;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.nodes.Replica;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.DefaultClientRequestPayload;
import byzzbench.simulator.transport.MessagePayload;
import lombok.ToString;
import lombok.extern.java.Log;

@Log
@ToString(callSuper = true)
public class FaultyLivenessReplica extends Replica {
    public FaultyLivenessReplica(String replicaId, Scenario scenario) {
        super(replicaId, scenario, new TotalOrderCommitLog());
    }

    @Override
    public void initialize() {
        // nothing to do
    }

    @Override
    public void handleMessage(String sender, MessagePayload m) {
        if (m instanceof DefaultClientRequestPayload clientRequestMessage) {
            this.broadcastMessage(clientRequestMessage);
        } else {
            throw new UnsupportedOperationException("Unknown message type: " + m.getType());
        }
    }

}

