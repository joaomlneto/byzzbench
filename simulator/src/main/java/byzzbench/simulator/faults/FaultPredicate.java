package byzzbench.simulator.faults;

import java.io.Serializable;
import java.util.function.Predicate;

public interface FaultPredicate extends Predicate<FaultInput>, Serializable {
    String getId();
    String getName();
}
