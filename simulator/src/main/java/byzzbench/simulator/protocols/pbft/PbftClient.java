package byzzbench.simulator.protocols.pbft;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.nodes.Client;
import byzzbench.simulator.protocols.pbft.message.RequestMessage;
import byzzbench.simulator.transport.DefaultClientReplyPayload;
import lombok.Getter;

import java.io.Serializable;

/**
 * Represents a client in the PBFT protocol.
 */
@Getter
public class PbftClient extends Client {
    public PbftClient(Scenario scenario, String id) {
        super(scenario, id);
    }

    /**
     * Sends a request to a replica in the system.
     */
    @Override
    public void sendRequest(String requestId, String senderId) {
        String recipientId = this.getRandomRecipientId();
        long sequenceNumber = this.getRequestSequenceNumber().get() - 1;
        // TODO: compute the digest
        RequestMessage request = new RequestMessage(this.getId(), sequenceNumber, "-1", requestId);
        this.getScenario().getTransport().sendMessage(this, request, recipientId);
    }

    @Override
    public boolean isRequestCompleted(DefaultClientReplyPayload message) {
        Serializable requestId = message.getRequestId();

        // Get the number of matching replies for the request ID
        long matchingReplies = this.getReplies().get(requestId).stream().filter(
                other -> other.equals(message.getReply())
        ).count();

        // Get the number of replicas in the scenario
        long numReplicas = this.getScenario().getReplicas().size();

        // In PBFT: n = 3f+1, therefore f = (n-1)/3
        // Client waits for f+1 matching replies before accepting the result
        // So we need at least (n-1)/3 + 1 matching replies!
        return matchingReplies >= ((numReplicas - 1) / 3) + 1;
    }
}
