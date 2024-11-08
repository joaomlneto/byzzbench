package byzzbench.simulator.transport.messages;

import byzzbench.simulator.transport.MessagePayload;

/**
 * Interface for {@link MessagePayload} that include a round number.
 */
public interface MessageWithRound {
  /**
   * Get the round number of the message.
   * @return The round number of the message.
   */
  public abstract long getRound();
}
