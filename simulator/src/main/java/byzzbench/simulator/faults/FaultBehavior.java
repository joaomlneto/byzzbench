package byzzbench.simulator.faults;

import java.io.Serializable;
import java.util.function.Consumer;

public interface FaultBehavior extends Consumer<FaultInput>, Serializable {
    String getId();
    String getName();
}
