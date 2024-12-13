package byzzbench.simulator.protocols.Zyzzyva;

import byzzbench.simulator.protocols.Zyzzyva.message.SpeculativeResponse;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public abstract class CommitCertificate implements Serializable {
    final long sequenceNumber;
    final long viewNumber;
    List<SpeculativeResponse> speculativeResponses;

    public CommitCertificate(long sequenceNumber, long viewNumber, List<SpeculativeResponse> speculativeResponses) {
        this.sequenceNumber = sequenceNumber;
        this.viewNumber = viewNumber;
        this.speculativeResponses = speculativeResponses;
    }
}
