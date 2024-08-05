package byzzbench.simulator.protocols.fasthotstuff.message;

import byzzbench.simulator.protocols.pbft_java.message.CheckpointMessage;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.With;

import java.util.Collection;

@Data
@With
public class PrepareMessage extends MessagePayload {
    private final String replicaId;
    private final long round;
    private final Collection<CheckpointMessage> quorumCertificate;

    @Override
    public String getType() {
        return "NEW-VIEW";
    }
}
