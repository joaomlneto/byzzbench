package byzzbench.simulator.protocols.event_driven_hotstuff.messages;

import byzzbench.simulator.protocols.event_driven_hotstuff.EDHSNode;
import lombok.Getter;

@Getter
public class GenericMessage extends AbstractMessage{
    EDHSNode node;

    public GenericMessage(long viewNumber, EDHSNode node) {
        super(MessageType.GENERIC, viewNumber);
        this.node = node;
    }
}
