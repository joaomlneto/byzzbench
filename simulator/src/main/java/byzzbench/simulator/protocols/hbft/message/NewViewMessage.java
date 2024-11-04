package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.util.Collection;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class NewViewMessage extends MessagePayload {
    private final long newViewNumber;
    private final Collection<ViewChangeMessage> viewChangeProofs;
    private final Collection<CheckpointIMessage> checkpointMessages;
    private final Collection<RequestMessage> speculativeHistory;

    @Override
    public String getType() {
        return "NEW-VIEW";
    }
}
