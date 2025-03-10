package byzzbench.simulator.versioning;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Represents a single entry in a {@link VectorClock}.
 * Each entry consists of a node identifier and a version number.
 * The version number is a positive integer.
 * The node identifier is a non-empty string.
 */
@Getter
@EqualsAndHashCode
@ToString
public final class VectorClockEntry implements Serializable, Cloneable {
    private final String nodeId;
    private final long version;

    public VectorClockEntry(String nodeId, long version) {
        if (nodeId == null || nodeId.isEmpty()) {
            throw new IllegalArgumentException("nodeId cannot be null");
        }

        if (version < 1) {
            throw new IllegalArgumentException("version must be positive");
        }

        this.nodeId = nodeId;
        this.version = version;
    }

    @Override
    public VectorClockEntry clone() {
        try {
            return (VectorClockEntry) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public VectorClockEntry incremented() {
        return new VectorClockEntry(nodeId, version + 1);
    }
}
