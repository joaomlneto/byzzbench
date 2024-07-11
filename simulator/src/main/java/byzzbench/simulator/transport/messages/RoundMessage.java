package byzzbench.simulator.transport.messages;

import byzzbench.simulator.transport.MessagePayload;

/**
 * Interface for {@link MessagePayload} that include a round number.
 */
public interface RoundMessage {
  long getRound();
}
