package byzzbench.simulator.state;

import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.transport.Event;

import java.io.Serializable;
import java.util.function.Predicate;

public class LivenessPredicate<T extends Serializable> implements Predicate<ScenarioExecutor<T>> {
    @Override
    public boolean test(ScenarioExecutor<T> scenarioExecutor) {
        return scenarioExecutor.getTransport().getEventsInState(Event.Status.QUEUED).isEmpty();
    }
}
