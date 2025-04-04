package byzzbench.simulator.protocols.pbft_java;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.Comparator;

@Data
public class ReplicaRequestKey implements Serializable, Comparable<ReplicaRequestKey> {
    private final String clientId;
    private final Instant timestamp;

    @Override
    public int compareTo(@NotNull ReplicaRequestKey other) {
        return Comparator.comparing(ReplicaRequestKey::getClientId)
                .thenComparing(ReplicaRequestKey::getTimestamp)
                .compare(this, other);
    }
}
