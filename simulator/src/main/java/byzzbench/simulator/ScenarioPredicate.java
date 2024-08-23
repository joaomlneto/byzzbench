package byzzbench.simulator;

import java.io.Serializable;
import java.util.function.Predicate;

public interface ScenarioPredicate extends Predicate<ScenarioExecutor>, Serializable {
}
