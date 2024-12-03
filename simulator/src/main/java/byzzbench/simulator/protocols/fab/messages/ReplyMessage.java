package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.protocols.fab.replicas.Pair;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * <p>Message sent by Acceptor replicas to new Leader replica to recover after new leader election.</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ReplyMessage extends MessagePayload {
    private final Pair valueAndProposalNumber;
    private final boolean isSigned;
    private final String sender;

    public String getType() {
        return "RESPONSE";
    }
}
