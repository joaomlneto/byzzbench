package byzzbench.simulator.protocols.event_driven_hotstuff.messages;

import byzzbench.simulator.protocols.event_driven_hotstuff.Node;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Getter;

@Getter
public abstract class AbstractMessage extends MessagePayload {
    MessageType type;

    public AbstractMessage(MessageType type) {
        this.type = type;
    }

    public MessageType getMessageType() {
        return type;
    }

    @Override
    public String getType() {
        return type.toString();
    }
}
