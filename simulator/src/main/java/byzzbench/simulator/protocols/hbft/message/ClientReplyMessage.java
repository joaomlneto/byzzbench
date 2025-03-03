package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClientReplyMessage extends MessagePayload {
    private final ReplyMessage reply;
    private final long tolerance;

    @Override
    public String getType() {
        return "ClientReplyMessage";
    }
}
