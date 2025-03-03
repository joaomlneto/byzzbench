package byzzbench.simulator.protocols.fab2;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ProposalNumber implements Comparable<ProposalNumber> {
    private final long viewNumber;
    private final long sequenceNumber;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final ProposalNumber other = (ProposalNumber) obj;
        return (this.viewNumber == other.viewNumber
                && this.sequenceNumber == other.sequenceNumber
        );
    }

    @Override
    public int compareTo(ProposalNumber o) {
        int result = Long.compare(this.viewNumber, o.viewNumber);
        if (result != 0) {
            return result;
        }

        return Long.compare(this.sequenceNumber, o.sequenceNumber);
    }
}
