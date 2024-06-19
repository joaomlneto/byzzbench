package byzzbench.simulator.protocols.pbft_java.message;

import byzzbench.simulator.transport.MessagePayload;

public interface IPhaseMessage extends MessagePayload {
    long getViewNumber();

    long getSequenceNumber();

    byte[] getDigest();
}
