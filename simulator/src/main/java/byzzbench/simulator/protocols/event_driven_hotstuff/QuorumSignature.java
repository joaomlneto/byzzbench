package byzzbench.simulator.protocols.event_driven_hotstuff;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

@Getter
@EqualsAndHashCode
public class QuorumSignature implements Serializable {
    private HashSet<PartialSignature> partialSignatures;

    public QuorumSignature(HashSet<PartialSignature> partialSignatures) {
        this.partialSignatures = partialSignatures;
    }

    public boolean isValid(String nodeHash, int minValidSignatures) {
        return partialSignatures.stream().filter(ps -> ps.getProposedNodeHash().equals(nodeHash)).count() == minValidSignatures;
    }
}
