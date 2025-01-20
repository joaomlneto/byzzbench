package byzzbench.simulator.protocols.event_driven_hotstuff;

import byzzbench.simulator.protocols.event_driven_hotstuff.messages.AbstractMessage;
import lombok.Getter;

import java.util.Collection;
import java.util.HashSet;

@Getter
public class IncompleteMessage {
    private final AbstractMessage message;
    private final String senderId;
    private final HashSet<String> missingNodes;

    public IncompleteMessage(AbstractMessage message, String senderId, Collection<String> missingNodes) {
        this.message = message;
        this.senderId = senderId;
        this.missingNodes = new HashSet<>(missingNodes);
    }
}
