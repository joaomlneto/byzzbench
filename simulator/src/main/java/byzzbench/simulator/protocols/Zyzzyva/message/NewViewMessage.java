package byzzbench.simulator.protocols.Zyzzyva.message;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.With;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class NewViewMessage extends MessagePayload implements MessageWithRound {
    private final long futureViewNumber;
    // ReplicaId -> ViewChangeMessage
    private final SortedMap<String, ViewChangeMessage> viewChangeMessages;
    // SequenceNumber -> OrderedRequestMessageWrapper
    private final SortedMap<Long, OrderedRequestMessageWrapper> orderedRequestHistory;

    @Override
    public long getRound() {
        return futureViewNumber - 1;
    }


    @Override
    public String getType() {
        return "NEW_VIEW";
    }
}
