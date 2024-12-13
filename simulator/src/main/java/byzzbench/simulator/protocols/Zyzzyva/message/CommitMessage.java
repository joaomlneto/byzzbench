package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.protocols.Zyzzyva.ClientCommitCertificate;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class CommitMessage extends MessagePayload {
    private final String clientId;
    private final ClientCommitCertificate commitCertificate;

    @Override
    public String getType() {
        return "COMMIT_MESSAGE";
    }
}