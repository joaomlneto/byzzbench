package byzzbench.simulator.protocols.tendermint.message;

import byzzbench.simulator.transport.MessagePayload;

import java.util.Comparator;

public abstract class GenericVoteMessage extends MessagePayload implements Comparable<GenericVoteMessage> {
    public abstract String getDigest();

    public abstract String getAuthor();

    public abstract long getHeight();

    @Override
    public int compareTo(GenericVoteMessage other) {
        return Comparator.comparing(GenericVoteMessage::getDigest)
                .thenComparing(GenericVoteMessage::getAuthor)
                .compare(this, other);
    }
}
