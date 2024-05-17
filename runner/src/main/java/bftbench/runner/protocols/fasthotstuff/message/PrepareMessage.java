package bftbench.runner.protocols.fasthotstuff.message;

import bftbench.runner.protocols.pbft.message.CheckpointMessage;
import bftbench.runner.transport.MessagePayload;
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
