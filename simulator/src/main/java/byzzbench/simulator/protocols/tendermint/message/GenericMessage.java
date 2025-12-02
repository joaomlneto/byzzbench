package byzzbench.simulator.protocols.tendermint.message;

import byzzbench.simulator.exploration_strategy.byzzfuzz.MessageWithByzzFuzzRoundInfo;
import byzzbench.simulator.transport.MessagePayload;

import java.util.Comparator;
import java.util.Objects;

public abstract class GenericMessage extends MessagePayload implements Comparable<GenericMessage>, MessageWithByzzFuzzRoundInfo {
    public abstract Block getBlock();

    public abstract long getRound();

    public abstract long getSequence();

    public abstract String getAuthor();

    public abstract long getHeight();

    public abstract String getType();

    @Override
    public int compareTo(GenericMessage other) {
        if (other == null) {
            return 1; // Non-null is greater than null
        }

        return Comparator.comparing(GenericMessage::getHeight)
                .thenComparing(GenericMessage::getSequence)
                .thenComparing(GenericMessage::getBlock, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(GenericMessage::getAuthor, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(GenericMessage::getType, Comparator.nullsFirst(Comparator.naturalOrder()))
                .compare(this, other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericMessage that = (GenericMessage) o;
        return getHeight() == that.getHeight() &&
                getSequence() == that.getSequence() &&
                getType().equals(that.getType()) &&
                getBlock().equals(that.getBlock()) &&
                getAuthor().equals(that.getAuthor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHeight(), getSequence(), getType(), getBlock(), getAuthor());
    }

}
