package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * <p>Message sent by Learner replicas to Learner replicas to request the other's learned value.</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class PullMessage extends MessagePayload {
    private final long viewNumber;

    public String getType() {
        return "PULL";
    }
}
