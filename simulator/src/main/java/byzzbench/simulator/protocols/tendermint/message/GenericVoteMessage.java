package byzzbench.simulator.protocols.tendermint.message;

import byzzbench.simulator.transport.MessagePayload;

import java.util.Comparator;

public abstract class GenericVoteMessage extends MessagePayload implements Comparable<GenericVoteMessage> {
    public abstract String getBlockHash();

    public abstract String getAuthor();

    public abstract long getHeight();

    @Override
    public int compareTo(GenericVoteMessage other) {
        return Comparator.comparing(GenericVoteMessage::getBlockHash)
                .thenComparing(GenericVoteMessage::getAuthor)
                .compare(this, other);
    }
}
