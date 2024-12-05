package byzzbench.simulator.protocols.basic_hotstuff.messages;

import byzzbench.simulator.protocols.basic_hotstuff.QuorumCertificate;

public class PreCommitMessage extends JustifiableMessage {
    public PreCommitMessage(int viewNumber, QuorumCertificate justify) {
        super(MessageType.PRE_COMMIT, viewNumber, justify);
    }
}
