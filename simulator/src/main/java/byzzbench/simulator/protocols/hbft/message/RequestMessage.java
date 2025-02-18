package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.With;

import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o instanceof RequestMessage message) {
            return this.operation.equals(message.getOperation())
                    && this.timestamp == message.getTimestamp()
                    && this.clientId.equals(message.getClientId());
        }
        
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, timestamp, clientId);
    }
}
