package byzzbench.simulator.protocols.fab.messages;

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
    private final long proposalNumber;

    @Override
    public String getType() {
        return "SUSPECT";
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
        return suspectId.getBytes();
    }
}

