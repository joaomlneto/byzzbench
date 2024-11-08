package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import byzzbench.simulator.protocols.hbft.SpeculativeHistory;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ViewChangeMessage extends MessagePayload {
    private final long newViewNumber;
    private final SpeculativeHistory speculativeHistoryP;
    private final SpeculativeHistory speculativeHistoryQ;
    private final SpeculativeHistory requestsR;
    private final String replicaId;

    @Override
    public String getType() {
        return "VIEW-CHANGE";
    }
}
