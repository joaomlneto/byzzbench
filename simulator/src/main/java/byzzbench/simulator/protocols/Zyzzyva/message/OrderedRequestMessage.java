package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.With;

import java.util.Arrays;

@Data
@With
public class OrderedRequestMessage extends MessagePayload {
    private final long viewNumber;
    private final long sequenceNumber;
    private final long history;
    private final byte[] digest;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OrderedRequestMessage other) {
            return this.viewNumber == other.viewNumber &&
                    this.sequenceNumber == other.sequenceNumber &&
                    this.history == other.history &&
                    Arrays.equals(this.digest, other.digest);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(viewNumber) ^
                Long.hashCode(sequenceNumber) ^
                Long.hashCode(history) ^
                Arrays.hashCode(digest);
    }

    @Override
    public String getType() {
        return "ORDERED_REQUEST";
    }
}