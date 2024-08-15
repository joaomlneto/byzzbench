package byzzbench.simulator.transport;

import java.io.Serializable;
import lombok.Data;

@Data
public class MutateMessageEventPayload implements Serializable {
  private final long eventId;
  private final String mutatorId;
}
