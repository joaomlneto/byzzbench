package byzzbench.simulator;

import java.io.Serializable;
import java.util.Comparator;
import java.util.function.Predicate;

public interface ScenarioPredicate extends Predicate<Scenario>, Serializable, Comparable<ScenarioPredicate> {
    default String getId() {
        return this.getClass().getSimpleName();
    }

    @Override
    default int compareTo(ScenarioPredicate other) {
        return Comparator.comparing(ScenarioPredicate::getId).compare(this, other);
    }
}
