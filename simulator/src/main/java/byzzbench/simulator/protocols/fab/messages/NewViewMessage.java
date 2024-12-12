package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class NewViewMessage extends MessagePayload {
    private final long viewNumber;

    public NewViewMessage(long viewNumber) {
        this.viewNumber = viewNumber;
    }

    @Override
    public String getType() {
        return "";
    }
}
