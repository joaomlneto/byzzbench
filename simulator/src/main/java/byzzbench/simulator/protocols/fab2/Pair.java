package byzzbench.simulator.protocols.fab2;
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
    private final ProposalNumber proposalNumber;

    public Pair(byte[] value, ProposalNumber proposalNumber) {
        this.value = value;
        this.proposalNumber = proposalNumber;
    }

    @Override
    public int compareTo(Pair o) {
        // Compare the proposal number of the pair
        return Long.compare(this.proposalNumber.getSequenceNumber(), o.proposalNumber.getSequenceNumber());
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
        return (this.proposalNumber.getViewNumber() == other.proposalNumber.getViewNumber()
                && Arrays.equals(this.value, other.value)
        );
    }
}
