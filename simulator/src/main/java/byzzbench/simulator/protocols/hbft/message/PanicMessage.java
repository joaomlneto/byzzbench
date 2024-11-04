package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class PanicMessage extends MessagePayload {
    private final byte[] digest;
    private final long timestamp;
    private final String clientId;

    @Override
    public String getType() {
        return "PANIC";
    }
}
