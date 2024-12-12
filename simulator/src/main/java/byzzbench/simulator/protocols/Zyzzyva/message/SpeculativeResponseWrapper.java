package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class SpeculativeResponseWrapper extends MessagePayload {
    private final SpeculativeResponse specResponse;
    private final String replicaId;
    private final Serializable reply;
    private final OrderedRequestMessage orderedRequest;

    @Override
    public String getType() {
        return "CLIENT_REPLY";
    }
}
