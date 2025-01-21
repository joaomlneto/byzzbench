package byzzbench.simulator.protocols.Zyzzyva.message;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class NewViewMessage extends MessagePayload {
    private final long futureViewNumber;
    // ReplicaId -> ViewChangeMessage
    private final SortedMap<String, ViewChangeMessage> viewChangeMessages;
    // SequenceNumber -> OrderedRequestMessageWrapper
    private final SortedMap<Long, OrderedRequestMessageWrapper> orderedRequestHistory;

    @Override
    public String getType() {
        return "NEW_VIEW";
    }
}
