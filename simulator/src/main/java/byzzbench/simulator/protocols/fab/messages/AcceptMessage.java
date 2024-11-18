package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class AcceptMessage extends MessagePayload {
    private final String replicaId;
    private final long round;
    private final String value;

    public String getType() {
        return "ACCEPT";
    }
}
