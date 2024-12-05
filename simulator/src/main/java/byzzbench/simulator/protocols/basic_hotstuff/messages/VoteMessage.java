package byzzbench.simulator.protocols.basic_hotstuff.messages;

import byzzbench.simulator.protocols.basic_hotstuff.Node;
import byzzbench.simulator.protocols.basic_hotstuff.PartialSignature;

public class VoteMessage extends AbstractMessage {
    PartialSignature partialSignature;

    public VoteMessage(MessageType type, int currentView, Node node, String senderId) {
        super(type, currentView, node);
        this.partialSignature = new PartialSignature(senderId);
    }
}
