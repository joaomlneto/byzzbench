package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.protocols.pbft_java.message.IPhaseMessage;
import byzzbench.simulator.transport.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * <p>Message sent by Learner replicas to Learner replicas to request the other's learned value.</p>
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class PullMessage extends IPhaseMessage {
    private final long proposalNumber;

    public String getType() {
        return "PULL";
    }

    @Override
    public long getViewNumber() {
        return proposalNumber;
    }

    @Override
    public long getSequenceNumber() {
        return proposalNumber;
    }

    @Override
    public byte[] getDigest() {
        return new byte[0];
    }
}
