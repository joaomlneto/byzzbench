package byzzbench.runner.protocols.fasthotstuff.message;

import byzzbench.runner.transport.MessagePayload;

public interface GenericVoteMessage extends MessagePayload {
    String getBlockHash();

    String getAuthor();
}
