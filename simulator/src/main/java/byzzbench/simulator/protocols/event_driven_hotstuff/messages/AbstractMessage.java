package byzzbench.simulator.protocols.event_driven_hotstuff.messages;

import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.Getter;

@Getter
public abstract class AbstractMessage extends MessagePayload implements MessageWithRound {
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

    @Override
    public long getRound() {
        return viewNumber;
    }
}
