package byzzbench.simulator.protocols.Zyzzyva;

import byzzbench.simulator.protocols.Zyzzyva.message.SpeculativeResponse;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CommitCertificate implements Serializable {
    final long sequenceNumber;
    final long viewNumber;
    List<String> replicaIds;
    List<SpeculativeResponse> speculativeResponses;
    String clientId;
}
