package byzzbench.simulator.transport.messages;

/**
 * Encodes information about the state of a protocol replica based on a message,
 * as per the ByzzFuzz algorithm
 * <a href="https://dl.acm.org/doi/10.1145/3586053">ByzzFuzz algorithm</a>
 */
public record ByzzFuzzRoundInfo(long viewNumber, long sequenceNumber,
                                int verbIndex) implements Comparable<ByzzFuzzRoundInfo> {
    @Override
    public int compareTo(ByzzFuzzRoundInfo o) {
        // if view numbers differ, compare by view number
        if (this.viewNumber != o.viewNumber) {
            return Long.compare(this.viewNumber, o.viewNumber);
        }

        // if sequence numbers differ, compare by sequence number
        if (this.sequenceNumber != o.sequenceNumber) {
            return Long.compare(this.sequenceNumber, o.sequenceNumber);
        }

        // otherwise, compare by verb index
        return Integer.compare(this.verbIndex, o.verbIndex);
    }
}
