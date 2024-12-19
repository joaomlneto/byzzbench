package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@With
public class ViewConfirmMessage extends MessagePayload {
    public final long futureViewNumber;
    public final long lastKnownSequenceNumber;
    public final long history;
    public final String replicaId;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ViewConfirmMessage other) {
            return this.futureViewNumber == other.futureViewNumber &&
                    this.lastKnownSequenceNumber == other.lastKnownSequenceNumber &&
                    this.history == other.history;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(futureViewNumber) ^
                Long.hashCode(lastKnownSequenceNumber) ^
                Long.hashCode(history);
    }

    @Override
    public String getType() {
        return "VIEW_CONFIRM";
    }

}
