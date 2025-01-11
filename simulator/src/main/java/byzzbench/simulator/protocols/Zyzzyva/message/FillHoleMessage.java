package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class FillHoleMessage extends MessagePayload {
    private final long viewNumber;
    private final long expectedSequenceNumber;
    private final long receivedSequenceNumber;
    private final String replicaId;


    @Override
    public String getType() {
        return "FILL_HOLE";
    }
}
