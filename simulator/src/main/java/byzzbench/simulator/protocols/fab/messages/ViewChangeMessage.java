package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.transport.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * <p>Message sent by a replica to indicate the election of a new leader.</p>
 */
@Getter
@AllArgsConstructor
@ToString
public class ViewChangeMessage extends MessagePayload {
    private final String senderId;
    private final long newViewNumber;
    private final String newLeaderId;

    public String getType() {
        return "VIEW_CHANGE";
    }
}
