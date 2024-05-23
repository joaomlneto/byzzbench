package bftbench.runner.protocols.fasthotstuff.message;

import bftbench.runner.transport.MessagePayload;

public interface GenericVoteMessage extends MessagePayload {
    String getBlockHash();

    String getAuthor();
}
