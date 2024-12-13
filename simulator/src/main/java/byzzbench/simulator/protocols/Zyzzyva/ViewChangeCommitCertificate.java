package byzzbench.simulator.protocols.Zyzzyva;

import byzzbench.simulator.protocols.Zyzzyva.message.SpeculativeResponse;
import lombok.Data;

import java.util.List;

@Data
public class ViewChangeCommitCertificate extends CommitCertificate {
    private final String replicaId;
    public ViewChangeCommitCertificate(long viewNumber, long sequenceNumber, List<SpeculativeResponse> speculativeResponses, String replicaId) {
        super(sequenceNumber, viewNumber, speculativeResponses);
        this.replicaId = replicaId;
        System.out.println("ViewChangeCommitCertificate constructor");
    }
}
