package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.With;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ViewChangeMessageWrapper extends MessagePayload implements MessageWithRound {
    private final List<IHateThePrimaryMessage> iHateThePrimaries;
    private final ViewChangeMessage viewChangeMessage;

    @Override
    public long getRound() {
        return (viewChangeMessage.getFutureViewNumber());
    }

    @Override
    public String getType() {
        return "VIEW_CHANGE";
    }

}
