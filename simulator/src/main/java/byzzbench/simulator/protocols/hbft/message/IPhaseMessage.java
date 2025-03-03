package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;

public abstract class IPhaseMessage extends MessagePayload implements MessageWithRound {
    /**
     * Get the view number of the message.
     *
     * @return The view number of the message.
     */
    public abstract long getViewNumber();

    /**
     * Get the sequence number of the message.
     *
     * @return The sequence number of the message.
     */
    public abstract long getSequenceNumber();

    /**
     * Get the digest of the message.
     *
     * @return The digest of the message.
     */
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
