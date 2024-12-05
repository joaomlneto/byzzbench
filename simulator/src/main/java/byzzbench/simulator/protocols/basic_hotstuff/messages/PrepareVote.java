package byzzbench.simulator.protocols.basic_hotstuff.messages;

import byzzbench.simulator.protocols.basic_hotstuff.Node;
import byzzbench.simulator.protocols.basic_hotstuff.PartialSignature;

public class PrepareVote extends VoteMessage {
    public PrepareVote(int currentView, Node node, String senderId) {
        super(MessageType.PREPARE_VOTE, currentView, node, senderId);
    }
}
