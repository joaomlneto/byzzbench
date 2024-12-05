package byzzbench.simulator.protocols.basic_hotstuff.messages;

import byzzbench.simulator.protocols.basic_hotstuff.Node;
import byzzbench.simulator.protocols.basic_hotstuff.PartialSignature;

public class PreCommitVote extends VoteMessage {
    public PreCommitVote(int currentView, Node node, String senderId) {
        super(MessageType.PRE_COMMIT_VOTE, currentView, node, senderId);
    }
}
