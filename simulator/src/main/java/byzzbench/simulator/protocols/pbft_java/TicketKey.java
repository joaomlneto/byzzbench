package byzzbench.simulator.protocols.pbft_java;

import java.io.Serializable;
import java.util.Comparator;
import lombok.Data;

@Data
public class TicketKey implements Serializable, Comparable<TicketKey> {
  private final long viewNumber;
  private final long seqNumber;

  @Override
  public int compareTo(TicketKey other) {
    return Comparator.comparing(TicketKey::getViewNumber)
        .thenComparingLong(TicketKey::getSeqNumber)
        .compare(this, other);
  }
}
