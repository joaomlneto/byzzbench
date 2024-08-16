package byzzbench.simulator.faults;

import java.io.Serializable;
import java.util.function.Predicate;

public interface FaultPredicate<T extends Serializable> extends Predicate<FaultInput<T>>, Serializable {
    String getId();
    String getName();
}
