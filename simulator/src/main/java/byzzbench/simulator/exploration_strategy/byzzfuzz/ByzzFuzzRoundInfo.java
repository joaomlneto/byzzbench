package byzzbench.simulator.exploration_strategy.byzzfuzz;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * Class representing the round information for ByzzFuzz exploration strategy.
 */
@Data
@AllArgsConstructor
public class ByzzFuzzRoundInfo implements Serializable, Comparable<ByzzFuzzRoundInfo> {
    private long viewNumber;
    private long sequenceNumber;
    private int verbIndex;

    @Override
    public int compareTo(ByzzFuzzRoundInfo other) {
        // Compare by view number
        if (this.viewNumber != other.viewNumber) {
            return Long.compare(this.viewNumber, other.viewNumber);
        }

        // Compare by sequence number
        if (this.sequenceNumber != other.sequenceNumber) {
            return Long.compare(this.sequenceNumber, other.sequenceNumber);
        }

        // Compare by verb index
        return Long.compare(this.verbIndex, other.verbIndex);
    }
}
