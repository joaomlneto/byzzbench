package byzzbench.runner.protocols.fasthotstuff.message;

import byzzbench.runner.protocols.pbft.message.CheckpointMessage;
import byzzbench.runner.transport.MessagePayload;
import lombok.Data;
import lombok.With;

import java.util.Collection;

@Data
@With
public class PrepareMessage implements MessagePayload {
    private final String replicaId;
    private final long round;
    private final Collection<CheckpointMessage> quorumCertificate;

    @Override
    public String getType() {
        return "NEW-VIEW";
    }
}
