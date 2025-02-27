package byzzbench.simulator.protocols.hbft.pojo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.Comparator;

@Data
public class ReplicaRequestKey implements Serializable, Comparable<ReplicaRequestKey> {
    private final String clientId;
    private final long timestamp;

    @Override
    public int compareTo(@NotNull ReplicaRequestKey other) {
        return Comparator.comparing(ReplicaRequestKey::getClientId)
                .thenComparingLong(ReplicaRequestKey::getTimestamp)
                .compare(this, other);
    }
}
