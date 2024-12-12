package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ConfirmRequestMessage extends MessagePayload {
    private final long viewNumber;
    public final RequestMessage requestMessage;
    public final String replicaId;

    @Override
    public String getType() {
        return "CONFIRM_REQUEST";
    }
}
