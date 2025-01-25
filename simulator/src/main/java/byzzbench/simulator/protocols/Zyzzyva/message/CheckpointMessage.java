package byzzbench.simulator.protocols.Zyzzyva.message;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.Data;
import lombok.With;

import java.util.Objects;

@Data
@With
public class CheckpointMessage extends MessagePayload implements MessageWithRound {
    private final long sequenceNumber;
    private final long history;
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
    public long getRound() {
        return (sequenceNumber - 1) * 10 + 6;
    }

    @Override
    public String getType() {
        return "CHECKPOINT";
    }
}
