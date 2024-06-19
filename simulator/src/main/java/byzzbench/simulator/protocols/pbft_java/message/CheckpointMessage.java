package byzzbench.simulator.protocols.pbft_java.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.With;

@Data
@With
public class CheckpointMessage implements MessagePayload {
    private final long lastSeqNumber;
    private final byte[] digest;
    private final String replicaId;

    @Override
    public String getType() {
        return "CHECKPOINT";
    }
}
