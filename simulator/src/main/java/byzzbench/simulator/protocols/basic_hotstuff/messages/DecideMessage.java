package byzzbench.simulator.protocols.basic_hotstuff.messages;

import byzzbench.simulator.protocols.basic_hotstuff.QuorumCertificate;

public class DecideMessage extends JustifiableMessage {
    public DecideMessage(int viewNumber, QuorumCertificate justify) {
        super(MessageType.DECIDE, viewNumber, justify);
    }
}
