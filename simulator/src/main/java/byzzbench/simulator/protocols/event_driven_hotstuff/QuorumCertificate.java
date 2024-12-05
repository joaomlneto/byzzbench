package byzzbench.simulator.protocols.event_driven_hotstuff;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
@EqualsAndHashCode
public class QuorumCertificate implements Serializable {
    private String nodeHash;
    private QuorumSignature signature;

    public QuorumCertificate(String nodeHash, QuorumSignature signature) {
        this.nodeHash = nodeHash;
        this.signature = signature;
    }

    public boolean isValid(String nodeHash, int minValidSignatures) {
        return nodeHash.equals("GENESIS") || signature.isValid(nodeHash, minValidSignatures);
    }
}
