package byzzbench.simulator.protocols.basic_hotstuff.messages;

import byzzbench.simulator.protocols.basic_hotstuff.Node;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Getter;

@Getter
public abstract class AbstractMessage extends MessagePayload {
    int currentView;
    MessageType type;
    Node node;

    public AbstractMessage(MessageType type, int currentView) {
        this.currentView = currentView;
        this.type = type;
    }

    public AbstractMessage(MessageType type, int currentView, Node node) {
        this.currentView = currentView;
        this.type = type;
        this.node = node;
    }

    public boolean matches(MessageType type, int viewNumber) {
        return this.type.equals(type) && (this.currentView == viewNumber);
    }

    public boolean matches(MessageType type, int viewNumber, Node node) {
        return matches(type, viewNumber) && this.node.equals(node);
    }

    @Override
    public String getType() {
        return type.toString();
    }
}
