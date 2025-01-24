package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.Data;
import lombok.With;

@Data
@With
public class OrderedRequestMessageWrapper extends MessagePayload implements Comparable<OrderedRequestMessageWrapper>
        , MessageWithRound {
//{
    private final OrderedRequestMessage orderedRequest;
    private final RequestMessage requestMessage;

    @Override
    public long getRound() {
        // there are two messages sent per round, the ORMW and the SRW
        return orderedRequest.getSequenceNumber() * 2 - 1;
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
