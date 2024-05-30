package byzzbench.simulator.protocols.fasthotstuff.message;

import byzzbench.simulator.transport.MessagePayload;

public interface GenericVoteMessage extends MessagePayload {
    String getBlockHash();

    String getAuthor();
}
