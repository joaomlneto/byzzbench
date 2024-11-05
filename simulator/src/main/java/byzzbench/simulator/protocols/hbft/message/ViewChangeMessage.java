package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.util.Collection;

import byzzbench.simulator.protocols.hbft.SpeculativeHistory;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ViewChangeMessage extends MessagePayload {
    private final long newViewNumber;
    private final Collection<CheckpointIMessage> qExecutions;
    private final Collection<CheckpointIIMessage> pExecutions;
    private final SpeculativeHistory speculativeHistory;
    private final String replicaId;

    @Override
    public String getType() {
        return "VIEW-CHANGE";
    }
}
