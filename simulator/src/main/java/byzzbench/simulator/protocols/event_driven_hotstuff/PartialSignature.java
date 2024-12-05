package byzzbench.simulator.protocols.event_driven_hotstuff;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
@EqualsAndHashCode
public class PartialSignature implements Serializable {
    String senderId;
    String proposedNodeHash;

    public PartialSignature(String senderId, String proposedNodeHash) {
        this.senderId = senderId;
        this.proposedNodeHash = proposedNodeHash;
    }
}
