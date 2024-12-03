package byzzbench.simulator.protocols.dummy;

import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.state.SerializableLogEntry;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import lombok.ToString;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

@Log
@ToString(callSuper = true)
public class DummyReplica extends Replica {

    /**
     * The current sequence number for the replica.
     */
    private final AtomicLong seqCounter = new AtomicLong(1);

    public DummyReplica(String replicaId, Scenario scenario) {
        super(replicaId, scenario, new TotalOrderCommitLog());
    }

    @Override
    public void initialize() {
        // nothing to do
    }

    @Override
    public void handleClientRequest(String clientId, long timestamp, Serializable request) {
        this.getCommitLog().add(seqCounter.incrementAndGet(), new SerializableLogEntry(request));
        this.broadcastMessage(new ClientRequestMessage(request));
    }

    @Override
    public void handleMessage(String sender, MessagePayload m) {
        if (m instanceof ClientRequestMessage clientRequestMessage) {
            this.getCommitLog().add(seqCounter.incrementAndGet(), new SerializableLogEntry(clientRequestMessage.getPayload()));
        } else {
            throw new UnsupportedOperationException("Unknown message type: " + m.getType());
        }
    }

}

