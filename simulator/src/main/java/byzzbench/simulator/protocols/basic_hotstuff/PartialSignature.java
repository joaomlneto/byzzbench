package byzzbench.simulator.protocols.basic_hotstuff;

import lombok.Getter;

@Getter
public class PartialSignature {
    String nodeId;

    public PartialSignature(String nodeId) {
        this.nodeId = nodeId;
    }
}
