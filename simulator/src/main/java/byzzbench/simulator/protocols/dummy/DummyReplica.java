package byzzbench.simulator.protocols.dummy;

import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.state.SerializableLogEntry;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import lombok.ToString;
import lombok.extern.java.Log;

import java.io.Serializable;

@Log
@ToString(callSuper = true)
public class DummyReplica extends Replica {

    public DummyReplica(String replicaId, Scenario scenario) {
        super(replicaId, scenario, new TotalOrderCommitLog());
    }

    @Override
    public void initialize() {
        // nothing to do
    }

    @Override
    public void handleClientRequest(String clientId, Serializable request) throws Exception {
        this.getCommitLog().add(new SerializableLogEntry(request));
        this.broadcastMessage(new ClientRequestMessage(request));
    }

    @Override
    public void handleMessage(String sender, MessagePayload m) {
        if (m instanceof ClientRequestMessage clientRequestMessage) {
            this.getCommitLog().add(new SerializableLogEntry(clientRequestMessage.getPayload()));
        } else {
            throw new UnsupportedOperationException("Unknown message type: " + m.getType());
        }
    }

}

