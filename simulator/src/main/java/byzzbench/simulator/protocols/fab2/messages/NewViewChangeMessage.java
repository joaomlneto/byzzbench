package byzzbench.simulator.protocols.fab2.messages;

import byzzbench.simulator.protocols.pbft_java.message.IPhaseMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class NewViewChangeMessage extends IPhaseMessage {
    @Getter
    private final long proposalNumber;
    private final String newLeaderId;

    public NewViewChangeMessage(long viewNumber, String newLeaderId) {
        this.proposalNumber = viewNumber;
        this.newLeaderId = newLeaderId;
    }

    @Override
    public long getViewNumber() {
        return proposalNumber;
    }

    @Override
    public String getType() {
        return "NEW_VIEW";
    }

    @Override
    public long getSequenceNumber() {
        return 0;
    }

    @Override
    public long getRound() {
        return 0;
    }

    @Override
    public byte[] getDigest() {
        return new byte[0];
    }
}
