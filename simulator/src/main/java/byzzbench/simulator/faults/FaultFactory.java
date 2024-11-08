package byzzbench.simulator.faults;

import java.util.List;

public interface FaultFactory {
  default String getId() { return this.getClass().getSimpleName(); }

  List<Fault> generateFaults(FaultContext input);
}
