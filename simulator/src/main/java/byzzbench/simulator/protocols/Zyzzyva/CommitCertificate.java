package byzzbench.simulator.protocols.Zyzzyva;

import byzzbench.simulator.protocols.Zyzzyva.message.SpeculativeResponse;
import lombok.Data;

import java.io.Serializable;
import java.util.SortedMap;

@Data
public class CommitCertificate implements Serializable {
    final long sequenceNumber;
    final long viewNumber;
    final long timestamp;
    final byte[] digest;
    SortedMap<String, SpeculativeResponse> speculativeResponses;
    String clientId;

    public CommitCertificate(long sequenceNumber, long viewNumber, long timestamp, byte[] digest, SortedMap<String, SpeculativeResponse> speculativeResponses) {
        this.sequenceNumber = sequenceNumber;
        this.viewNumber = viewNumber;
        this.timestamp = timestamp;
        this.digest = digest;
        this.speculativeResponses = speculativeResponses;
    }
}
