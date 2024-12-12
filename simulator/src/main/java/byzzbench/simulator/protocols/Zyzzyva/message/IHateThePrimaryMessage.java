package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class IHateThePrimaryMessage extends MessagePayload {
    private final long viewNumber;

    @Override
    public String getType() {
        return "I_HATE_THE_PRIMARY";
    }
}
