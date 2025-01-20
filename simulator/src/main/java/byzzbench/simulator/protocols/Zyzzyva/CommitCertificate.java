package byzzbench.simulator.protocols.Zyzzyva;

import byzzbench.simulator.protocols.Zyzzyva.message.SpeculativeResponse;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CommitCertificate implements Serializable {
    final long sequenceNumber;
    final long viewNumber;
    final long history;
    SpeculativeResponse speculativeResponse;
    List<String> signedBy;

    public CommitCertificate(long sequenceNumber,
                             long viewNumber,
                             long history,
                             SpeculativeResponse speculativeResponse,
                             List<String> signedBy) {
        this.sequenceNumber = sequenceNumber;
        this.viewNumber = viewNumber;
        this.history = history;
        this.speculativeResponse = speculativeResponse;
        this.signedBy = signedBy;
    }
}
