package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.util.Collection;

import byzzbench.simulator.protocols.hbft.SpeculativeHistory;
import byzzbench.simulator.protocols.hbft.utils.Checkpoint;
import byzzbench.simulator.transport.messages.MessageWithRound;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class NewViewMessage extends MessagePayload implements MessageWithRound {
    private final long newViewNumber;
    private final Collection<ViewChangeMessage> viewChangeProofs;
    private final Checkpoint checkpoint;
    private final SpeculativeHistory speculativeHistory;

    @Override
    public String getType() {
        return "NEW-VIEW";
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
