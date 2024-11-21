package byzzbench.simulator.protocols.fab.replicas;

import lombok.Getter;
import lombok.extern.java.Log;

/**
 * A <h1>Pair</h1> class that representing the value accepted by acceptors.
 *
 */
@Log
@Getter
public class Pair implements Comparable<Pair> {
    private final byte[] value;
    private final long number;

    public Pair(long number, byte[] value) {
        this.value = value;
        this.number = number;
    }

    @Override
    public int compareTo(Pair o) {
        // Compare the proposal number of the pair
        return Long.compare(this.number, o.number);
    }
}
