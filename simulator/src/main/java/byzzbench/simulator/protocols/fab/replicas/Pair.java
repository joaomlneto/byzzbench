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
    private final String value;
    private final String number;

    public Pair(String number, String value) {
        this.value = value;
        this.number = number;
    }

    @Override
    public int compareTo(Pair o) {
        return value.compareTo(o.value);
    }
}
