package bftbench.runner.protocols.fasthotstuff.message;

import bftbench.runner.transport.MessagePayload;

public interface GenericVoteMessage extends MessagePayload {
    byte[] getBlockHash();

    String getAuthor();
}
