package byzzbench.simulator.scheduler;

public class EventDecision {
    private final DecisionType decision;
    private final long eventId;

    public EventDecision(DecisionType decision_, long eventId_) {
        this.decision = decision_;
        this.eventId = eventId_;
    }

    public DecisionType getDecision() {
        return decision;
    }

    public long getEventId() {
        return eventId;
    }

    public enum DecisionType {
        DELIVERED,
        DROPPED,
        MUTATED
    }
}
