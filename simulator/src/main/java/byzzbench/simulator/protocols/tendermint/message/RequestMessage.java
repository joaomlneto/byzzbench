package byzzbench.simulator.protocols.tendermint.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.With;

import java.io.Serializable;
import java.util.Comparator;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class RequestMessage extends MessagePayload {
    @Getter
    private final Serializable operation;
    private final long timestamp;
    private final String clientId;

    @Override
    public String toString() {
        return "RequestMessage{" +
                "operation=" + operation +
                ", timestamp=" + timestamp +
                ", clientId='" + clientId + '\'' +
                '}';
    }

    @Override
    public String getType() {
        return "REQUEST";
    }
}
