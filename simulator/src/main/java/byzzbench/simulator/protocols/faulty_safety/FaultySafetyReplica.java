package byzzbench.simulator.protocols.faulty_safety;

import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.state.SerializableLogEntry;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.DefaultClientRequestPayload;
import byzzbench.simulator.transport.MessagePayload;
import lombok.ToString;
import lombok.extern.java.Log;

@Log
@ToString(callSuper = true)
public class FaultySafetyReplica extends Replica {
    public FaultySafetyReplica(String replicaId, Scenario scenario) {
        super(replicaId, scenario, new TotalOrderCommitLog());
    }

    @Override
    public void initialize() {
        // nothing to do
    }

    @Override
    public void handleMessage(String sender, MessagePayload m) {
        if (m instanceof DefaultClientRequestPayload clientRequestMessage) {
            this.getCommitLog().add(new SerializableLogEntry(clientRequestMessage.getOperation()));
            this.broadcastMessage(clientRequestMessage);
        } else {
            throw new UnsupportedOperationException("Unknown message type: " + m.getType());
        }
    }

}

