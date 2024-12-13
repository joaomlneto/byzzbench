package byzzbench.simulator.protocols.event_driven_hotstuff;

import byzzbench.simulator.Client;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.transport.DefaultClientReplyPayload;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Getter;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;

@Getter
public class EDHotStuffClient extends Client {
    HashMap<String, ClientRequest> pendingRequests;

    private final long maxRequests;
    private boolean initialized;

    public EDHotStuffClient(Scenario scenario, String id) {
        super(scenario, id);
        initialized = false;
        this.maxRequests = Long.MAX_VALUE;
    }

    public EDHotStuffClient(Scenario scenario, String id, int maxRequests) {
        super(scenario, id);
        initialized = false;
        this.maxRequests = maxRequests;
    }

    @Override
    public void handleMessage(String senderId, MessagePayload replyMessage) {
        if (replyMessage instanceof DefaultClientReplyPayload replyPayload) {
            Serializable reply = replyPayload.getReply();
            if (reply instanceof ClientReply clientReply) {
                String requestId = clientReply.getRequestId();
                pendingRequests.remove(requestId);

                this.getReplies().add(reply);
                if (this.getRequestSequenceNumber().get() < this.maxRequests) {
                    this.sendRequest();
                }
            }
        }
    }

    @Override
    public void sendRequest() {
        String requestId = String.format("%s/%d", this.getId(), this.getRequestSequenceNumber().getAndIncrement());

        ClientRequest clientRequest = new ClientRequest(requestId, this.getId(), "cmd/" + requestId);
        pendingRequests.put(requestId, clientRequest);
        resendRequest(clientRequest);
    }

    private void resendRequest(ClientRequest clientRequest) {
        if (pendingRequests.containsKey(clientRequest.getRequestId())) {
            sendRequestToAll(clientRequest);
            /*
            setTimeout(
                    clientRequest.getRequestId() + " resend timeout",
                    () -> resendRequest(clientRequest),
                    Duration.ofSeconds(30)
            );*/
        }
    }

    private void sendRequestToAll(ClientRequest request) {
        this.getScenario().getNodes().keySet().forEach(recipientId ->
                this.getScenario().getTransport().sendClientRequest(this.getId(), request, recipientId)
        );
    }

    @Override
    public void initialize() {
        if(initialized) return;
        initialized = true;

        pendingRequests = new HashMap<>();

        this.sendRequest();
        this.sendRequest();
        this.sendRequest();
        this.sendRequest();
    }
}
