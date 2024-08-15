package byzzbench.simulator.protocols.fasthotstuff.faults;

import byzzbench.simulator.faults.Fault;
import java.util.ArrayList;
import java.util.List;

public class FHSBugFaults {
  public List<Fault> getFaults() {
    List<Fault> faults = new ArrayList<>();
    // faults.add(new OmitMessage("B", 4, Block.class));
    return faults;
  }
}
