package byzzbench.simulator.protocols.basic_hotstuff.messages;

import byzzbench.simulator.protocols.basic_hotstuff.Node;
import byzzbench.simulator.protocols.basic_hotstuff.QuorumCertificate;

public class PrepareMessage extends JustifiableMessage {
    public PrepareMessage(int viewNumber, QuorumCertificate justify, Node node) {
        super(MessageType.PREPARE, viewNumber, justify, node);
    }
}
