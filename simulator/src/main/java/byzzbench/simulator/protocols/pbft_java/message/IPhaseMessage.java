package byzzbench.simulator.protocols.pbft_java.message;

import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;

/**
 * Superclass for all messages that are part of the regular PBFT protocol phases.
 */
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
