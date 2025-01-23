package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class OrderedRequestMessageWrapper extends MessagePayload implements Comparable<OrderedRequestMessageWrapper>
        , MessageWithRound {
//{
    private final OrderedRequestMessage orderedRequest;
    private final RequestMessage requestMessage;

    @Override
    public long getRound() {
        return orderedRequest.getSequenceNumber();
    }
    @Override
    public int compareTo(OrderedRequestMessageWrapper o) {
        return orderedRequest.compareTo(o.getOrderedRequest());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OrderedRequestMessageWrapper other) {
            return orderedRequest.equals(other.getOrderedRequest()) &&
                    requestMessage.equals(other.getRequestMessage());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return orderedRequest.hashCode() ^ requestMessage.hashCode();
    }

    @Override
    public String getType() {
        return "ORDERED_REQUEST";
    }
}
