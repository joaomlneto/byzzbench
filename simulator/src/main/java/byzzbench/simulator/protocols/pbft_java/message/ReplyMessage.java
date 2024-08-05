package byzzbench.simulator.protocols.pbft_java.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ReplyMessage extends MessagePayload {
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
