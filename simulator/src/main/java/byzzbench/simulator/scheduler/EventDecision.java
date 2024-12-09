package byzzbench.simulator.scheduler;

import byzzbench.simulator.utils.NonNull;
import lombok.Data;

@Data
public class EventDecision {
    @NonNull
    private final DecisionType decision;
    @NonNull
    private final long eventId;

    public enum DecisionType {
        DELIVERED,
        DROPPED,
        MUTATED_AND_DELIVERED
    }
}
