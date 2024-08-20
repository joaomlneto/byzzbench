package byzzbench.simulator.scheduler;

import lombok.Data;

@Data
public class EventDecision {
    private final DecisionType decision;
    private final long eventId;

    public enum DecisionType {
        DELIVERED,
        DROPPED,
        MUTATED
    }
}
