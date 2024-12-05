package byzzbench.simulator.protocols.basic_hotstuff;

import byzzbench.simulator.protocols.basic_hotstuff.messages.MessageType;
import lombok.Data;
import lombok.Getter;

@Getter
public class QuorumCertificate {
    private MessageType type;
    private int viewNumber;
    private Node node;
    private QuorumSignature signature;

    public QuorumCertificate(MessageType type, int viewNumber, Node node, QuorumSignature signature) {
        this.type = type;
        this.viewNumber = viewNumber;
        this.node = node;
        this.signature = signature;
    }

    public boolean match(MessageType type, int viewNumber) {
        return this.type.equals(type) && this.viewNumber == viewNumber;
    }
}
