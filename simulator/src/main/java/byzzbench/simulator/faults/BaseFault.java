package byzzbench.simulator.faults;

import byzzbench.simulator.domain.Action;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * A base fault implementation that provides a name, predicate, and behavior.
 */
@Data
public class BaseFault extends Fault {
    private final String id;
    @JsonIgnore
    private final FaultPredicate predicate;
    @JsonIgnore
    private final FaultBehavior behavior;

    public String getName() {
        return behavior.getName() + " when " + predicate.getName();
    }

    @Override
    public Action toAction(ScenarioContext context) {
        return this.behavior.toAction(context);
    }

    @Override
    public boolean test(ScenarioContext state) {
        return predicate.test(state);
    }

    public void accept(ScenarioContext state) {
        behavior.toAction(state).accept(state.getScenario());
    }
}
