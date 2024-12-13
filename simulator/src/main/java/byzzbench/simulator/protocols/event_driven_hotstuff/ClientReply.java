package byzzbench.simulator.protocols.event_driven_hotstuff;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
@EqualsAndHashCode
public class ClientReply implements Serializable {
    private final String senderId;
    private final String requestId;
    private final Serializable replyMessage;

    public ClientReply(String senderId, String requestId, Serializable replyMessage) {
        this.senderId = senderId;
        this.requestId = requestId;
        this.replyMessage = replyMessage;
    }
}
