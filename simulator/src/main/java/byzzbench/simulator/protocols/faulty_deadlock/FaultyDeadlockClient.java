package byzzbench.simulator.protocols.faulty_deadlock;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.nodes.Client;
import byzzbench.simulator.nodes.ClientReply;
import byzzbench.simulator.nodes.ClientRequestMessage;
import byzzbench.simulator.transport.MessagePayload;

public class FaultyDeadlockClient extends Client {
    public FaultyDeadlockClient(Scenario scenario, String id) {
        super(scenario, id);
    }

    @Override
    public boolean isRequestCompleted(ClientReply message) {
        return true;
    }

    @Override
    public void sendRequest(String requestId, String recipientId) {
        MessagePayload payload = new ClientRequestMessage(requestId, this.getCurrentTime().toEpochMilli(), requestId);
        this.getScenario().getTransport().sendMessage(this, payload, recipientId);

        // same as default client but with no retransmission
        //this.setTimeout(String.format("Request %s", requestId), this::retransmitRequest, this.timeout);
    }
}
