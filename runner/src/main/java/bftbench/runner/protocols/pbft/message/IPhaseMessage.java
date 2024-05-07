package bftbench.runner.protocols.pbft.message;

import bftbench.runner.transport.MessagePayload;

public interface IPhaseMessage extends MessagePayload {
    long getViewNumber();

    long getSequenceNumber();

    byte[] getDigest();
}
