package byzzbench.simulator.protocols.event_driven_hotstuff.messages;

import byzzbench.simulator.protocols.event_driven_hotstuff.EDHSNode;
import lombok.Getter;

@Getter
public class TellMessage extends AbstractMessage {
    private EDHSNode node;

    public TellMessage(long viewNumber, EDHSNode node) {
        super(MessageType.TELL, viewNumber);
        this.node = node;
    }
}
