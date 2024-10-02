package byzzbench.simulator;

import java.io.Serializable;
import java.util.function.Predicate;

public interface ScenarioPredicate extends Predicate<BaseScenario>,
                                           Serializable,
                                           Comparable<ScenarioPredicate> {
  default String getId() { return this.getClass().getSimpleName(); }

  @Override
  default int compareTo(ScenarioPredicate o) {
    return this.getId().compareTo(o.getId());
  }
}
