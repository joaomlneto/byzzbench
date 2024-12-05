package byzzbench.simulator.protocols.event_driven_hotstuff.messages;

import byzzbench.simulator.protocols.event_driven_hotstuff.Node;
import byzzbench.simulator.protocols.event_driven_hotstuff.PartialSignature;
import lombok.Getter;

@Getter
public class GenericVote extends GenericMessage {
    PartialSignature partialSignature;

    public GenericVote(Node node, String senderId) {
        super(node);
        this.partialSignature = new PartialSignature(senderId, node.getHash());
        this.type = MessageType.GENERIC_VOTE;
    }
}
