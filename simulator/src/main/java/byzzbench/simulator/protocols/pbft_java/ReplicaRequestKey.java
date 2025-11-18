package byzzbench.simulator.protocols.pbft_java;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;
import java.util.Comparator;

public record ReplicaRequestKey(String clientId,
                                Instant timestamp) implements Serializable, Comparable<ReplicaRequestKey> {
    @Override
    public int compareTo(@NotNull ReplicaRequestKey other) {
        int result = Comparator.comparing(ReplicaRequestKey::clientId)
                .thenComparing(ReplicaRequestKey::timestamp)
                .compare(this, other);
        System.out.println("Comparing ReplicaRequestKey: " + this + " with " + other + " => " + result);
        return result;
    }
}
