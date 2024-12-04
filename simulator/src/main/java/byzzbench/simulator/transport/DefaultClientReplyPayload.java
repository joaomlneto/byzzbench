package byzzbench.simulator.transport;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Represents a client request with a given operation.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DefaultClientReplyPayload extends MessagePayload {
    private final Serializable reply;

    @Override
    public String getType() {
        return "DefaultClientRequest";
    }
}
