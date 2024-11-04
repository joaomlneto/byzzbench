package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.transport.MessagePayload;

public abstract class IPhaseMessage extends MessagePayload {
    public abstract long getViewNumber();

    public abstract long getSequenceNumber();

    public abstract byte[] getDigest();
    
}
