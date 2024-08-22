package byzzbench.simulator.transport;

import byzzbench.simulator.utils.NonNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class MutateMessageEventPayload implements Serializable {
    @NonNull
    private final long eventId;
    @NonNull
    private final String mutatorId;
}
