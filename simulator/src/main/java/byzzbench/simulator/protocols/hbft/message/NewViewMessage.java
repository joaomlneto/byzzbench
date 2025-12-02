package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.exploration_strategy.byzzfuzz.MessageWithByzzFuzzRoundInfo;
import byzzbench.simulator.protocols.hbft.SpeculativeHistory;
import byzzbench.simulator.protocols.hbft.utils.Checkpoint;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.util.Collection;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class NewViewMessage extends MessagePayload implements MessageWithByzzFuzzRoundInfo {
    private final long newViewNumber;
    private final Collection<ViewChangeMessage> viewChangeProofs;
    private final Checkpoint checkpoint;
    private final SpeculativeHistory speculativeHistory;

    @Override
    public String getType() {
        return "NEW-VIEW";
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
