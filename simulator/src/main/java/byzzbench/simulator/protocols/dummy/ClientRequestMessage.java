package byzzbench.simulator.protocols.dummy;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;

import java.io.Serializable;

@Data
public class ClientRequestMessage extends MessagePayload {
    private final Serializable payload;
    @Override
    public String getType() {
        return "ClientRequestMessage";
    }
}
