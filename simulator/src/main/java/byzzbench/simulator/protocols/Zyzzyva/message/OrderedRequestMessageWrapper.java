package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class OrderedRequestMessageWrapper extends MessagePayload {
    private final OrderedRequestMessage orderedRequest;
    private final RequestMessage requestMessage;

    @Override
    public String getType() {
        return "ORDERED_REQUEST";
    }
}
