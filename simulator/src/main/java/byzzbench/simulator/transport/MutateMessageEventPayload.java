package byzzbench.simulator.transport;

import byzzbench.simulator.utils.NonNull;
import java.io.Serializable;
import lombok.Data;

@Data
public class MutateMessageEventPayload implements Serializable {
  @NonNull private final long eventId;
  @NonNull private final String mutatorId;
}
