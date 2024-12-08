package byzzbench.simulator.protocols.hbft;

import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.state.CommitLog;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import lombok.ToString;
import lombok.extern.java.Log;

import java.io.Serializable;

/**
 * A Replica in the hBFT protocol.
 * <p>
 * Based on the publication by S. Duan, S. Peisert and K. N. Levitt:
 * hBFT: Speculative Byzantine Fault Tolerance with Minimum Cost
 */
@Log
@ToString(callSuper = true)
public class HbftReplica extends Replica {
    /**
     * TODO: Replica attributes go here, such as the current sequence number, the current view, etc.
     * <p>
     * Remember to use {@link CommitLog} to store the state of the replica!
     * To do this, use the {@link #commitOperation} method to append an entry to the log.
     */
    private final int sequenceNumber = 0; // TODO

    /**
     * Create a new replica.
     *
     * @param nodeId   the unique ID of the replica
     * @param scenario the scenario
     */
    protected HbftReplica(String nodeId, Scenario scenario) {
        super(nodeId, scenario, new TotalOrderCommitLog());
    }

    @Override
    public void initialize() {
        // TODO
    }

    @Override
    public void handleClientRequest(String clientId, Serializable request) throws Exception {
        // TODO
    }

    @Override
    public void handleMessage(String sender, MessagePayload m) {
        // TODO
    }

}

