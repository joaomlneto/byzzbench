package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.Data;
import lombok.With;

import java.util.Arrays;

@Data
@With
//<SpecResponse, v, n, hn, H(r), c, t>
public class SpeculativeResponse extends MessagePayload

//        implements MessageWithRound {
{
    private final long viewNumber;
    private final long sequenceNumber;
    private final long history;
    private final byte[] replyDigest;
    private final String clientId;
    private final long timestamp;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SpeculativeResponse other) {
            return this.viewNumber == other.viewNumber &&
                    this.sequenceNumber == other.sequenceNumber &&
                    this.history == other.history &&
                    Arrays.equals(this.replyDigest, other.replyDigest) &&
                    this.clientId.equals(other.clientId) &&
                    this.timestamp == other.timestamp;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(viewNumber) ^
                Long.hashCode(sequenceNumber) ^
                Long.hashCode(history) ^
                Arrays.hashCode(replyDigest) ^
                clientId.hashCode() ^
                Long.hashCode(timestamp);
    }

//    @Override
//    public long getRound() {
//        return sequenceNumber;
//    }

    @Override
    public String getType() {
        return "SPECULATIVE_RESPONSE";
    }
}