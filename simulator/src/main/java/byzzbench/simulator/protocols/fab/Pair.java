package byzzbench.simulator.protocols.fab;
import lombok.Data;
import lombok.extern.java.Log;

import java.util.Arrays;

/**
 * A <h1>Pair</h1> class that representing the value accepted by acceptors.
 *
 */
@Log
@Data
public class Pair implements Comparable<Pair> {
    private final byte[] value;
    private final Long number;

    public Pair(long number, byte[] value) {
        this.value = value;
        this.number = number;
    }

    @Override
    public int compareTo(Pair o) {
        // Compare the proposal number of the pair
        return Long.compare(this.number, o.number);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final Pair other = (Pair) obj;
        return (this.number == other.getNumber() && Arrays.equals(this.value, other.value));
    }
}
