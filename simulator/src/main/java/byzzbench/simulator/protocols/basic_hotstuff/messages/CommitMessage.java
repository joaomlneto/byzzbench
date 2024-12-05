package byzzbench.simulator.protocols.basic_hotstuff.messages;

import byzzbench.simulator.protocols.basic_hotstuff.QuorumCertificate;

public class CommitMessage extends JustifiableMessage {
    public CommitMessage(int viewNumber, QuorumCertificate justify) {
        super(MessageType.COMMIT, viewNumber, justify);
    }
}
