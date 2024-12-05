package byzzbench.simulator.protocols.basic_hotstuff.messages;

import byzzbench.simulator.protocols.basic_hotstuff.Node;
import byzzbench.simulator.protocols.basic_hotstuff.QuorumCertificate;

public class NewViewMessage extends JustifiableMessage {
    public NewViewMessage( int viewNumber, QuorumCertificate justify) {
        super(MessageType.NEW_VIEW, viewNumber, justify);
    }

    public NewViewMessage(int viewNumber, QuorumCertificate justify, Node node) {
        super(MessageType.NEW_VIEW, viewNumber, justify, node);
    }
}
