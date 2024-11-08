package byzzbench.simulator.protocols.pbft;

public interface CertifiableMessage {
  /**
   * Checks if this message matches another message.
   *
   * @param other the other message.
   * @return true if the messages match, false otherwise.
   */
  boolean match(CertifiableMessage other);

  /**
   * Returns the identifier of the principal that sent the message
   *
   * @return the principal identifier
   */
  String id();

  /**
   * Check if message is authenticated and correct
   *
   * @return true iff the message is properly authenticated and statically
   *     correct
   */
  boolean verify();

  /**
   * Check if message is full
   *
   * @return true iff the message is full
   */
  boolean full();

  /**
   * Encodes the message.
   *
   * @return true if the message was successfully encoded, false otherwise.
   */
  boolean encode();

  /**
   * Decodes the message.
   *
   * @return true if the message was successfully decoded, false otherwise.
   */
  boolean decode();
}
