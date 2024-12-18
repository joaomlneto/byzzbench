package byzzbench.simulator.protocols.event_driven_hotstuff;

import byzzbench.simulator.protocols.event_driven_hotstuff.messages.AbstractMessage;
import lombok.Getter;

@Getter
public class IncompleteMessage {
    private final AbstractMessage message;
    private final String senderId;

    public IncompleteMessage(AbstractMessage message, String senderId) {
        this.message = message;
        this.senderId = senderId;
    }
}
