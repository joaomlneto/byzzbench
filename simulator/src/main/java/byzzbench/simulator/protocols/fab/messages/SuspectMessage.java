package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.transport.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * <p>Message sent by proposers to indicate suspicion of the leader.</p>
 */
@Getter
@AllArgsConstructor
@ToString
public class SuspectMessage extends MessagePayload {
    private final String senderId;
    private final String suspectId;

    @Override
    public String getType() {
        return "SUSPECT";
    }
}

