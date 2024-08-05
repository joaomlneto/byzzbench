package byzzbench.simulator.protocols.pbft_java.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.With;

import java.util.Collection;
import java.util.Map;

@Data
@With
public class ViewChangeMessage extends MessagePayload {
    private final long newViewNumber;
    private final long lastSeqNumber;
    private final Collection<CheckpointMessage> checkpointProofs;
    private final Map<Long, Collection<IPhaseMessage>> preparedProofs;
    private final String replicaId;

    @Override
    public String getType() {
        return "VIEW-CHANGE";
    }
}
