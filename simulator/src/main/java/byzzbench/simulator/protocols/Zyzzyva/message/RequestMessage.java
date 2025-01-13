package byzzbench.simulator.protocols.Zyzzyva.message;

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
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RequestMessage that = (RequestMessage) o;
        return timestamp == that.timestamp && Objects.equals(operation, that.operation) && clientId.equals(that.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, timestamp, clientId);
    }

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