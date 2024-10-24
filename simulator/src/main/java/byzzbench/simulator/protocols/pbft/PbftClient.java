package byzzbench.simulator.protocols.pbft;

import byzzbench.simulator.Client;
import byzzbench.simulator.protocols.pbft.message.RequestMessage;
import byzzbench.simulator.transport.Transport;

/**
 * Represents a client in the PBFT protocol.
 */
public class PbftClient extends Client {
    public PbftClient(String clientId, Transport transport) {
        super(clientId, transport);
    }

    /**
     * Sends a request to a replica in the system.
     */
    @Override
    public void sendRequest() {
        String recipientId = this.getTransport().getScenario().getNodes().keySet().iterator().next();
        long sequenceNumber = this.getRequestSequenceNumber().getAndIncrement();
        String command = String.format("%s/%d", this.getClientId(), sequenceNumber);
        // TODO: compute the digest
        RequestMessage request = new RequestMessage(this.getClientId(), sequenceNumber, "-1", command);
        this.getTransport().sendClientRequest(this.getClientId(), request, recipientId);
    }
}
