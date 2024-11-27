package byzzbench.simulator.transport;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a client request with a given operation.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DefaultClientRequestPayload extends MessagePayload {
    private final long timestamp;
    private final Serializable operation;

    @Override
    public String getType() {
        return "DefaultClientRequest";
    }
}
