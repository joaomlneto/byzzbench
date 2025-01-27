package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.protocols.Zyzzyva.CommitCertificate;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class CommitMessage extends MessagePayload implements MessageWithRound {
    private final String clientId;
    private final CommitCertificate commitCertificate;

    @Override public long getRound() {
        return (commitCertificate.getSequenceNumber() - 1) * 10 + 3;
//        return commitCertificate.getViewNumber();
    }

    @Override
    public String getType() {
        return "COMMIT_MESSAGE";
    }
}