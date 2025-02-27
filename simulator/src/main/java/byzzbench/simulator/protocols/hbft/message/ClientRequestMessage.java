package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClientRequestMessage extends MessagePayload {
    private final long timestamp;
    private final Serializable operation;

    @Override
    public String getType() {
        return "ClientRequest";
    }
}
