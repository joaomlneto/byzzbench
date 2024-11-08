package byzzbench.simulator.protocols.pbft.message;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A Data message: see Data.h/cc.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class BFTDataMessage extends DataMessage {
  /**
   * Total size of the object
   */
  private final int totalSize;

  /**
   * Current fragment number
   */
  private final int chunkNo;

  public BFTDataMessage(int index, long lastModifiedSeqno, byte[] data,
                        int totalSize, int chunkNo) {
    super(index, lastModifiedSeqno, data);
    this.totalSize = totalSize;
    this.chunkNo = chunkNo;
  }
}
