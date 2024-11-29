package byzzbench.simulator.protocols.tendermint.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
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
