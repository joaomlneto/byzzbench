package byzzbench.simulator.protocols.tendermint.message;

import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;

import java.util.Comparator;
import java.util.Objects;

public abstract class GenericMessage extends MessagePayload implements Comparable<GenericMessage>, MessageWithRound {
    public abstract Block getBlock();

    public abstract long getRound();

    public abstract String getAuthor();

    public abstract long getHeight();

    public abstract String getType();

    @Override
    public int compareTo(GenericMessage other) {
        if (other == null) {
            return 1; // Non-null is greater than null
        }

        return Comparator.comparing(GenericMessage::getHeight)
                .thenComparing(GenericMessage::getRound)
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
                getRound() == that.getRound() &&
                getType().equals(that.getType()) &&
                getBlock().equals(that.getBlock()) &&
                getAuthor().equals(that.getAuthor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHeight(), getRound(), getType(), getBlock(), getAuthor());
    }

}
