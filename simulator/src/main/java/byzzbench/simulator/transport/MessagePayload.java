package byzzbench.simulator.transport;

import java.io.Serializable;

/**
 * Interface for the payload of a {@link MessageEvent}.
 */
public interface MessagePayload extends Serializable {
  String getType();
}
