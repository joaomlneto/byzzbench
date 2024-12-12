package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ViewConfirmMessage extends MessagePayload {
    public final long futureViewNumber;
    public final long lastKnownSequenceNumber;
    public final long history;
    public final String replicaId;

    @Override
    public String getType() {
        return "VIEW_CONFIRM";
    }

}
