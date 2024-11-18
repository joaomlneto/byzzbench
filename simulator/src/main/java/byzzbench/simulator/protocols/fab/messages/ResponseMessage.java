package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ResponseMessage extends MessagePayload {
    public String getType() {
        return "RESPONSE";
    }
}
