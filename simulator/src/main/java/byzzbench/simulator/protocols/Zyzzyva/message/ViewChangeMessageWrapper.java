package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ViewChangeMessageWrapper extends MessagePayload {
    private final List<IHateThePrimaryMessage> iHateThePrimaries;
    private final ViewChangeMessage viewChangeMessage;

    @Override
    public String getType() {
        return "VIEW_CHANGE";
    }

}
