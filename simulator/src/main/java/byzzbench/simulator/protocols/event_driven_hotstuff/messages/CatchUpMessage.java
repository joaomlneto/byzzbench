package byzzbench.simulator.protocols.event_driven_hotstuff.messages;

import byzzbench.simulator.protocols.event_driven_hotstuff.EDHSNode;
import lombok.Getter;

@Getter
public class CatchUpMessage extends AbstractMessage {
    private EDHSNode node;

    public CatchUpMessage(long viewNumber, EDHSNode node) {
        super(MessageType.CATCH_UP, viewNumber);
        this.node = node;
    }
}
