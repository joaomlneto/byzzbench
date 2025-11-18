package byzzbench.simulator.protocols.pbft_java;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.nodes.Client;
import byzzbench.simulator.nodes.ClientReply;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class PbftClient extends Client {
    public PbftClient(Scenario scenario, String id) {
        super(scenario, id);
    }

    @Override
    public boolean isRequestCompleted(ClientReply message) {
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
        boolean isCompleted = matchingReplies >= ((numReplicas - 1) / 3) + 1;

        System.out.println("Is request " + requestId + " completed? " + isCompleted + " (matchingReplies: " + matchingReplies + ", numReplicas: " + numReplicas + ")");

        return isCompleted;
    }
}
