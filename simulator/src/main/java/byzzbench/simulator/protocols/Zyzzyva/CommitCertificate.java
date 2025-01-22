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
    final byte[] digest;
    SpeculativeResponse speculativeResponse;
    List<String> signedBy;

    public CommitCertificate(long sequenceNumber,
                             long viewNumber,
                             long history,
                             byte[] digest,
                             SpeculativeResponse speculativeResponse,
                             List<String> signedBy) {
        this.sequenceNumber = sequenceNumber;
        this.viewNumber = viewNumber;
        this.history = history;
        this.digest = digest;
        this.speculativeResponse = speculativeResponse;
        this.signedBy = signedBy;
    }
}
