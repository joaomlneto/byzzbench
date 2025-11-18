package byzzbench.simulator.transport;

import byzzbench.simulator.nodes.ClientReply;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Represents a client request with a given operation.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DefaultClientReplyPayload extends MessagePayload implements ClientReply {
    private final Serializable requestId;
    private final Serializable reply;

    @Override
    public String getType() {
        return "DefaultClientRequest";
    }
}
