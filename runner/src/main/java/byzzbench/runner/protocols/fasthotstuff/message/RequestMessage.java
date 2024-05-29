package byzzbench.runner.protocols.fasthotstuff.message;

import byzzbench.runner.transport.MessagePayload;
import lombok.Data;
import lombok.With;

import java.io.Serializable;

@Data
@With
public class RequestMessage implements MessagePayload {
    private final Serializable operation;
    private final long timestamp;
    private final String clientId;

    @Override
    public String getType() {
        return "REQUEST";
    }
}
