package byzzbench.simulator.protocols.fasthotstuff.message;

import byzzbench.simulator.transport.MessagePayload;

public abstract class GenericVoteMessage extends MessagePayload {
    public abstract String getBlockHash();

    public abstract String getAuthor();
}
