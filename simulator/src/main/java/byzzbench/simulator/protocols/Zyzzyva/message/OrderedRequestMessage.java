package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class OrderedRequestMessage extends MessagePayload {
    private final long viewNumber;
    private final long sequenceNumber;
    private final long historyHash;
    private final byte[] digest;

    @Override
    public String getType() {
        return "ORDERED_REQUEST";
    }
}