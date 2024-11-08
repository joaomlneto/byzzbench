package byzzbench.simulator.protocols.pbft_java;

import java.io.Serializable;
import lombok.Data;

@Data
public class ViewChangeResult implements Serializable {
  private final boolean shouldBandwagon;
  private final long bandwagonViewNumber;
  private final boolean beginNextVote;
}
