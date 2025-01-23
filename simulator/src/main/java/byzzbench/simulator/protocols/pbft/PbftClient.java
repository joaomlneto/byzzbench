package byzzbench.simulator.protocols.pbft;

import byzzbench.simulator.Client;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.protocols.pbft.message.RequestMessage;

/**
 * Represents a client in the PBFT protocol.
 */
public class PbftClient extends Client {
    public PbftClient(Scenario scenario, String clientId) {
        super(scenario, clientId);
    }

    /**
     * Sends a request to a replica in the system.
     */
    @Override
    public void sendRequest() {
        String recipientId = this.getScenario().getReplicas().keySet().iterator().next();
        long sequenceNumber = this.getRequestSequenceNumber().getAndIncrement();
        String command = String.format("%s/%d", this.getId(), sequenceNumber);
        // TODO: compute the digest
        RequestMessage request = new RequestMessage(this.getId(), sequenceNumber, "-1", command);
        this.getScenario().getTransport().sendClientRequest(this.getId(), request, recipientId, System.currentTimeMillis());
    }
}
