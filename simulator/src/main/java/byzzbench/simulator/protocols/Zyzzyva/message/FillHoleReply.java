package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.With;

import java.util.Objects;

@Data
@With
public class FillHoleReply extends MessagePayload {
    private final OrderedRequestMessage orderedRequestMessage;
    private final RequestMessage requestMessage;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FillHoleReply that = (FillHoleReply) o;
        return Objects.equals(orderedRequestMessage, that.orderedRequestMessage) && Objects.equals(requestMessage, that.requestMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderedRequestMessage, requestMessage);
    }

    @Override
    public String getType() {
        return "FILL_HOLE_WRAPPER";
    }
}
