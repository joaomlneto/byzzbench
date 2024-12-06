package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.protocols.fab.ProgressCertificate;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * <p>Message sent by Proposer replicas to Acceptor replicas to recover after new leader election.</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class QueryMessage extends MessagePayload {
    private final long viewNumber;
    private final ProgressCertificate progressCertificate;

    public String getType() {
        return "QUERY";
    }
}
