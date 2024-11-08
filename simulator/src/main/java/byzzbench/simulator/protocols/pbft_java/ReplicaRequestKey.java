package byzzbench.simulator.protocols.pbft_java;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Comparator;
import lombok.Data;

@Data
public class ReplicaRequestKey
    implements Serializable, Comparable<ReplicaRequestKey> {
  private final String clientId;
  private final long timestamp;

  @Override
  public int compareTo(@NotNull ReplicaRequestKey other) {
    return Comparator.comparing(ReplicaRequestKey::getClientId)
        .thenComparingLong(ReplicaRequestKey::getTimestamp)
        .compare(this, other);
  }
}
