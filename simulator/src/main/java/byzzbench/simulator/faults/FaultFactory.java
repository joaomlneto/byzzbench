package byzzbench.simulator.faults;

import java.util.List;

public interface FaultFactory {
  List<Fault> generateFaults(FaultContext input);
}
