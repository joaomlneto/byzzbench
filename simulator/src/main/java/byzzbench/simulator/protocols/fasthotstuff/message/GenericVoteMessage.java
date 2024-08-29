package byzzbench.simulator.protocols.fasthotstuff.message;

import byzzbench.simulator.transport.MessagePayload;

public abstract class GenericVoteMessage extends MessagePayload implements Comparable<GenericVoteMessage> {
    public abstract String getBlockHash();

    public abstract String getAuthor();

    @Override
    public int compareTo(GenericVoteMessage o) {
        if (o == this) {
            return 0;
        }

        int authorCompare = this.getAuthor().compareTo(o.getAuthor());
        if (authorCompare != 0) {
            return authorCompare;
        }

        return this.getBlockHash().compareTo(o.getBlockHash());
    }
}
