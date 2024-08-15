package byzzbench.simulator.faults;

import java.io.Serializable;
import java.util.function.Consumer;

public interface FaultBehavior<T extends Serializable>
    extends Consumer<FaultInput<T>>, Serializable {
  String getId();
  String getName();
}
