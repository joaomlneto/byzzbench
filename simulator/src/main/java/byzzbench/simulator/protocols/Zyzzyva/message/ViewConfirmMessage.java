package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;

import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.Data;
import lombok.With;

@Data
@With
public class ViewConfirmMessage extends MessagePayload implements Comparable<ViewConfirmMessage>, MessageWithRound {
    public final long futureViewNumber;
    public final long lastKnownSequenceNumber;
    public final long history;
    public final String replicaId;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ViewConfirmMessage other) {
            return this.futureViewNumber == other.futureViewNumber &&
                    this.lastKnownSequenceNumber == other.lastKnownSequenceNumber &&
                    this.history == other.history &&
                    this.replicaId.equals(other.replicaId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(futureViewNumber) ^
                Long.hashCode(lastKnownSequenceNumber) ^
                Long.hashCode(history) ^
                replicaId.hashCode();
    }

    @Override
    public long getRound() {
        return (lastKnownSequenceNumber - 1) * 10 + 10;
    }

    @Override
    // compare the replicaId of the two messages
    public int compareTo(ViewConfirmMessage other) {
        return this.replicaId.compareTo(other.replicaId);
    }

    @Override
    public String getType() {
        return "VIEW_CONFIRM";
    }

}
