package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.transport.MessagePayload;

public interface IPhaseMessage extends MessagePayload {
    long getViewNumber();

    long getSequenceNumber();

    byte[] getDigest();
}
