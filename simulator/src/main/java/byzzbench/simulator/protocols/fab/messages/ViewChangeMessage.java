package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.transport.MessagePayload;
import lombok.*;

/**
 * <p>Message sent by a replica to indicate the election of a new leader.</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ViewChangeMessage extends MessagePayload {
    private final String senderId;
    private final long proposalNumber;
    private final String newLeaderId;

    public String getType() {
        return "VIEW_CHANGE";
    }
}
