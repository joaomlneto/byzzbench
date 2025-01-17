package byzzbench.simulator.protocols.fab2.messages;

import byzzbench.simulator.protocols.fab2.ProposalNumber;
import byzzbench.simulator.protocols.pbft_java.message.IPhaseMessage;
import byzzbench.simulator.transport.MessagePayload;
import lombok.*;

/**
 * <p>Message sent by proposers to indicate suspicion of the leader.</p>
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class SuspectMessage extends IPhaseMessage {
    private final String senderId;
    private final String suspectId;
    private final ProposalNumber proposalNumber;

    @Override
    public String getType() {
        return "SUSPECT";
    }

    @Override
    public long getViewNumber() {
        return proposalNumber.getViewNumber();
    }

    @Override
    public long getSequenceNumber() {
        return proposalNumber.getSequenceNumber();
    }

    @Override
    public byte[] getDigest() {
        return suspectId.getBytes();
    }
}

