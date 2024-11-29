package byzzbench.simulator.protocols.tendermint.message;

import byzzbench.simulator.transport.MessagePayload;

import java.util.Comparator;

public abstract class GenericMessage extends MessagePayload implements Comparable<GenericMessage> {
    public abstract Block getBlock();

    public abstract long getRound();

    public abstract String getAuthor();

    public abstract long getHeight();

    @Override
    public int compareTo(GenericMessage other) {
        return Comparator.comparing(GenericMessage::getBlock, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(GenericMessage::getAuthor)
                .compare(this, other);
    }

}
