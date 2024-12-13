package byzzbench.simulator.protocols.event_driven_hotstuff.messages;

import byzzbench.simulator.protocols.event_driven_hotstuff.EDHSNode;
import byzzbench.simulator.protocols.event_driven_hotstuff.PartialSignature;
import lombok.Getter;

@Getter
public class GenericVote extends AbstractMessage {
    EDHSNode node;
    PartialSignature partialSignature;

    public GenericVote(long viewNumber, EDHSNode node, String senderId) {
        super(MessageType.GENERIC_VOTE, viewNumber);
        this.node = node;
        this.partialSignature = new PartialSignature(senderId, node.getHash());
    }
}
