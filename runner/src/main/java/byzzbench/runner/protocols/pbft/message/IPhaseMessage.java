package byzzbench.runner.protocols.pbft.message;

import byzzbench.runner.transport.MessagePayload;

public interface IPhaseMessage extends MessagePayload {
    long getViewNumber();

    long getSequenceNumber();

    byte[] getDigest();
}
