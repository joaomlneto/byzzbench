package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;

public abstract class IPhaseMessage extends MessagePayload implements MessageWithRound {
    public abstract long getViewNumber();

    public abstract long getSequenceNumber();

    public abstract byte[] getDigest();

    /**
     * Get the request of the message.
     *
     * @return The request of the message.
     */
    public long getRound() {
        return getSequenceNumber();
    }
    
}
