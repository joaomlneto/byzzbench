package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.protocols.hbft.SpeculativeHistory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.io.Serializable;

import byzzbench.simulator.transport.messages.MessageWithRound;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ReplyMessage extends MessagePayload implements MessageWithRound{
    private final long viewNumber;
    private final long timestamp;
    private final long sequenceNumber;
    private final String clientId;
    private final String replicaId;
    private final Serializable result;
    // Speculative execution history
    private final SpeculativeHistory speculativeHistory;

    @Override
    public String getType() {
        return "REPLY";
    }

    @Override
    public long getRound() {
        return this.sequenceNumber * 3;
    }
}
