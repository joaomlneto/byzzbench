package byzzbench.simulator.protocols.event_driven_hotstuff;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;

@Getter
@EqualsAndHashCode
public class QuorumSignature implements Serializable {
    private ArrayList<PartialSignature> partialSignatures;

    public QuorumSignature(ArrayList<PartialSignature> partialSignatures) {
        this.partialSignatures = partialSignatures;
    }

    public boolean isValid(String nodeHash, int minValidSignatures) {
        return partialSignatures.stream().filter(ps -> ps.getProposedNodeHash().equals(nodeHash)).count() == minValidSignatures;
    }
}
