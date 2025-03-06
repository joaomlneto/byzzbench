package byzzbench.simulator.faults;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * A base fault implementation that provides a name, predicate, and behavior.
 */
@Data
public class BaseFault implements Fault {
    private final String id;
    @JsonIgnore
    private final FaultPredicate predicate;
    @JsonIgnore
    private final FaultBehavior behavior;

    public String getName() {
        return behavior.getName() + " when " + predicate.getName();
    }

    @Override
    public boolean test(ScenarioContext state) {
        return predicate.test(state);
    }

    @Override
    public void accept(ScenarioContext state) {
        behavior.accept(state);
    }
}
