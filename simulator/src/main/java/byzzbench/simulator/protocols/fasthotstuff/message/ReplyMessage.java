package byzzbench.simulator.protocols.fasthotstuff.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.With;

import java.io.Serializable;

@Data
@With
public class ReplyMessage implements MessagePayload {
    private final long viewNumber;
    private final long timestamp;
    private final String clientId;
    private final String replicaId;
    private final Serializable result;

    @Override
    public String getType() {
        return "REPLY";
    }
}
