package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class FillHoleReply extends MessagePayload {
    private final OrderedRequestMessage orderedRequestMessage;
    private final RequestMessage requestMessage;

    @Override
    public String getType() {
        return "FILL_HOLE_WRAPPER";
    }
}
