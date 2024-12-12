package byzzbench.simulator.protocols.Zyzzyva.message;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.util.Collection;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class NewViewMessage extends MessagePayload {
    private final long futureViewNumber;
    private final Collection<ViewChangeMessage> viewChangeMessages;

    @Override
    public String getType() {
        return "NEW_VIEW";
    }
}
