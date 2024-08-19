package byzzbench.simulator.transport;

import lombok.Data;
import org.springframework.lang.NonNull;

import java.io.Serializable;

@Data
public class MutateMessageEventPayload implements Serializable {
    @NonNull
    private final long eventId;
    @NonNull
    private final String mutatorId;
}
