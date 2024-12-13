package byzzbench.simulator.protocols.event_driven_hotstuff.messages;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Getter;

@Getter
public abstract class AbstractMessage extends MessagePayload {
    long viewNumber;
    MessageType type;

    public AbstractMessage(MessageType type, long viewNumber) {
        this.type = type;
        this.viewNumber = viewNumber;
    }

    public MessageType getMessageType() {
        return type;
    }

    @Override
    public String getType() {
        return type.toString();
    }
}
