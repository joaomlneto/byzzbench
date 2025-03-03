package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class NewViewChangeMessage extends MessagePayload {
    private final long viewNumber;
    private final String newLeaderId;

    public NewViewChangeMessage(long viewNumber, String newLeaderId) {
        this.viewNumber = viewNumber;
        this.newLeaderId = newLeaderId;
    }

    @Override
    public String getType() {
        return "NEW_VIEW";
    }
}
