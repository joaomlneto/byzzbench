package byzzbench.simulator.protocols.faulty_safety;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.nodes.ClientRequest;
import byzzbench.simulator.nodes.Replica;
import byzzbench.simulator.state.SerializableLogEntry;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import lombok.ToString;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Log
@ToString(callSuper = true)
public class FaultySafetyReplica extends Replica {
    private final Set<Serializable> seenOperations = new HashSet<>();

    public FaultySafetyReplica(String replicaId, Scenario scenario) {
        super(replicaId, scenario, new TotalOrderCommitLog());
    }

    @Override
    public void initialize() {
        // nothing to do
    }

    @Override
    public void handleMessage(String sender, MessagePayload m) {
        if (m instanceof ClientRequest clientRequestMessage) {
            if (!seenOperations.contains(clientRequestMessage.getOperation())) {
                System.out.println("Replica " + this.getId()
                        + " broadcasting operation: " + clientRequestMessage.getOperation());
                this.getCommitLog().add(new SerializableLogEntry(clientRequestMessage.getOperation()));
                this.broadcastMessage(m);
                seenOperations.add(clientRequestMessage.getOperation());
            }
        } else {
            throw new UnsupportedOperationException("Unknown message type: " + m.getType());
        }
    }

}

