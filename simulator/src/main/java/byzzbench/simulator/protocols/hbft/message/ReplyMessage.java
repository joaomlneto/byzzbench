package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.exploration_strategy.byzzfuzz.MessageWithByzzFuzzRoundInfo;
import byzzbench.simulator.protocols.hbft.SpeculativeHistory;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.io.Serializable;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ReplyMessage extends MessagePayload implements MessageWithByzzFuzzRoundInfo {
    private final long viewNumber;
    private final Instant timestamp;
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
