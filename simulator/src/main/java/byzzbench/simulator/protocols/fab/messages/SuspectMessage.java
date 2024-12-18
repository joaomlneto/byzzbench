package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.transport.MessagePayload;
import lombok.*;

/**
 * <p>Message sent by proposers to indicate suspicion of the leader.</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class SuspectMessage extends MessagePayload {
    private final String senderId;
    private final String suspectId;
    private final long viewNumber;

    @Override
    public String getType() {
        return "SUSPECT";
    }
}

