package byzzbench.simulator.nodes;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClientRequestMessage extends MessagePayload implements ClientRequest {
    private final Serializable requestId;
    private final long timestamp;
    private final Serializable operation;

    @Override
    public String getType() {
        return "Client Request";
    }
}
