package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
//<<SpecResponse, v, n, hn, H(r), c, t>i, r, OR>
public class SpeculativeResponse extends MessagePayload {

    private final long viewNumber;
    private final long sequenceNumber;
    private final long history;
    private final byte[] replyDigest;
    private final String clientId;
    private final long timestamp;

    @Override
    public String getType() {
        return "SPECULATIVE_RESPONSE";
    }
}