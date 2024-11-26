package byzzbench.simulator.protocols.pbft;

/**
 * Interface for PBFT messages that have a sequence number.
 */
public interface PbftMessagePayloadWithSequenceNumber {
    /**
     * Returns the sequence number associated with this message.
     *
     * @return the sequence number of the message
     */
    long seqno();

    /**
     * Verifies the message.
     *
     * @return true if the message is valid, false otherwise
     */
    boolean verify();

    /**
     * Returns the view number associated with this message.
     *
     * @return the view number of the message
     */
    long view();
}
