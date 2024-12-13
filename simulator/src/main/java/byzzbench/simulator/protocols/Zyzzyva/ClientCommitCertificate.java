package byzzbench.simulator.protocols.Zyzzyva;

import byzzbench.simulator.protocols.Zyzzyva.message.SpeculativeResponse;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ClientCommitCertificate extends CommitCertificate {
    List<String> replicaIds;
    String clientId;

    public ClientCommitCertificate(long sequenceNumber, long viewNumber, List<SpeculativeResponse> speculativeResponses, List<String> replicaIds, String clientId) {
        super(sequenceNumber, viewNumber, speculativeResponses);
        this.replicaIds = replicaIds;
        this.clientId = clientId;
    }
}