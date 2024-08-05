package byzzbench.simulator.protocols.pbft_java.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.With;

import java.io.Serializable;

@Data
@With
public class RequestMessage extends MessagePayload {
    private final Serializable operation;
    private final long timestamp;
    private final String clientId;

    @Override
    public String getType() {
        return "REQUEST";
    }
}
