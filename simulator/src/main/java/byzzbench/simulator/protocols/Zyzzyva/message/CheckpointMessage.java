package byzzbench.simulator.protocols.Zyzzyva.message;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.With;

import java.util.Objects;

@Data
@With
public class CheckpointMessage extends MessagePayload {
    private final long sequenceNumber;
    private final long history;
    /// TODO: check if we need to add application state?
    private final String replicaId;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CheckpointMessage that = (CheckpointMessage) o;
        return sequenceNumber == that.sequenceNumber && history == that.history;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequenceNumber, history);
    }

    @Override
    public String getType() {
        return "CHECKPOINT";
    }
}
