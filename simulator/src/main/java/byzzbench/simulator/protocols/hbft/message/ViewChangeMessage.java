package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.exploration_strategy.byzzfuzz.MessageWithByzzFuzzRoundInfo;
import byzzbench.simulator.protocols.hbft.SpeculativeHistory;
import byzzbench.simulator.protocols.hbft.utils.Checkpoint;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.util.SortedMap;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ViewChangeMessage extends MessagePayload implements MessageWithByzzFuzzRoundInfo {
    private final long newViewNumber;
    private final SpeculativeHistory speculativeHistoryP;
    private final Checkpoint speculativeHistoryQ;
    private final SortedMap<Long, RequestMessage> requestsR;
    private final String replicaId;

    @Override
    public String getType() {
        return "VIEW-CHANGE";
    }

    @Override
    public long getViewNumber() {
        return this.newViewNumber;
    }

    @Override
    public long getRound() {
        return getNewViewNumber();
    }
}
