package byzzbench.simulator.protocols.basic_hotstuff.messages;

import byzzbench.simulator.protocols.basic_hotstuff.Node;
import byzzbench.simulator.protocols.basic_hotstuff.PartialSignature;

public class CommitVote extends VoteMessage {
    public CommitVote(int currentView, Node node, String senderId) {
        super(MessageType.COMMIT_VOTE, currentView, node, senderId);
    }
}
