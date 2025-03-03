package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.protocols.pbft_java.message.IPhaseMessage;
import byzzbench.simulator.transport.MessagePayload;
import lombok.*;

/**
 * <p>Message sent by a replica to indicate the election of a new leader.</p>
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ViewChangeMessage extends IPhaseMessage {
    private final String senderId;
    private final long proposalNumber;
    private final String newLeaderId;

    public String getType() {
        return "VIEW_CHANGE";
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
        return newLeaderId.getBytes();
    }
}
