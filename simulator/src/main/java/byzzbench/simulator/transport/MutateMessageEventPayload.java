package byzzbench.simulator.transport;

import lombok.Data;

import java.io.Serializable;

@Data
public class MutateMessageEventPayload implements Serializable {
    private final long eventId;
    private final long mutatorId;
    private final String payloadBefore;
    private final String payloadAfter;
}
