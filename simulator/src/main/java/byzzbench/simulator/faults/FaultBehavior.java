package byzzbench.simulator.faults;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.domain.Action;
import byzzbench.simulator.utils.NonNull;

import java.io.Serializable;

public interface FaultBehavior extends Serializable {
    @NonNull
    String getId();

    @NonNull
    String getName();

    @NonNull
    Action toAction(ScenarioContext context);

    @NonNull
    default Action toAction(Scenario scenario) {
        return toAction(new ScenarioContext(scenario));
    }
}
