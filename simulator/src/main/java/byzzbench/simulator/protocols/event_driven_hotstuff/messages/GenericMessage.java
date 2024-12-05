package byzzbench.simulator.protocols.event_driven_hotstuff.messages;

import byzzbench.simulator.protocols.event_driven_hotstuff.Node;
import lombok.Getter;

@Getter
public class GenericMessage extends AbstractMessage{
    Node node;

    public GenericMessage(Node node) {
        super(MessageType.GENERIC);
        this.node = node;
    }
}
