package byzzbench.simulator.transport;

import java.io.Serializable;
import lombok.Data;
import org.springframework.lang.NonNull;

@Data
public class MutateMessageEventPayload implements Serializable {
  @NonNull private final long eventId;
  @NonNull private final String mutatorId;
}
