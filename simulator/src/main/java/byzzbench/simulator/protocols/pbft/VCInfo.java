package byzzbench.simulator.protocols.pbft;

import java.io.Serializable;
import java.util.BitSet;
import lombok.Data;

/**
 * The VCI (View Change Information) structure. see NV_info.h/cc. ?
 */
@Data
public class VCInfo implements Serializable {
  /**
   * The view change number?
   */
  private final long vc;

  /**
   * The number of acknowledgements?
   */
  private final long ackCount;

  /**
   * The number of acknowledgements received?
   */
  private final BitSet ackReps;

  /**
   * The request summary?
   */
  private final boolean reqSum;
}
