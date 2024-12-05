package byzzbench.simulator.protocols.basic_hotstuff.messages;

import byzzbench.simulator.protocols.basic_hotstuff.Node;
import byzzbench.simulator.protocols.basic_hotstuff.QuorumCertificate;
import lombok.Getter;

@Getter
public class JustifiableMessage extends AbstractMessage {
    QuorumCertificate justify;

    public JustifiableMessage(MessageType type, int viewNumber,QuorumCertificate justify) {
        super(type, viewNumber);
        this.justify = justify;
    }

    public JustifiableMessage(MessageType type, int viewNumber, QuorumCertificate justify, Node node) {
        super(type, viewNumber, node);
        this.justify = justify;
    }
}
