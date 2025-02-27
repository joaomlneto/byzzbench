package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.util.SortedMap;

import byzzbench.simulator.protocols.hbft.SpeculativeHistory;
import byzzbench.simulator.protocols.hbft.utils.Checkpoint;
import byzzbench.simulator.transport.messages.MessageWithRound;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ViewChangeMessage extends MessagePayload implements MessageWithRound {
    private final long newViewNumber;
    private final SpeculativeHistory speculativeHistoryP;
    private final Checkpoint speculativeHistoryQ;
    private final SortedMap<Long, RequestMessage> requestsR;
    private final String replicaId;

    @Override
    public String getType() {
        return "VIEW-CHANGE";
    }

    /**
     * Get the request of the message.
     *
     * @return The request of the message.
     */
    public long getRound() {
        return getNewViewNumber();
    }
}
