package byzzbench.simulator.protocols.hbft.utils;

import byzzbench.simulator.protocols.hbft.SpeculativeHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Checkpoint implements Comparable<Checkpoint>, Serializable {
    private long sequenceNumber;
    private SpeculativeHistory history;

    @Override
    public boolean equals(Object o) {
        if (o instanceof Checkpoint other) {
            return !(other.history != this.history || other.sequenceNumber != this.sequenceNumber);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(sequenceNumber, history);
    }

    @Override
    public int compareTo(Checkpoint other) {
        // Compare first by sequenceNumber, then by history (if sequence numbers are equal)
        int sequenceComparison = Long.compare(this.sequenceNumber, other.sequenceNumber);
        if (sequenceComparison != 0) {
            return sequenceComparison;
        }
        // If sequenceNumber is the same, compare by history (assuming history is comparable)
        return this.history.compareTo(other.history); // Assuming SpeculativeHistory implements Comparable
    }
}
