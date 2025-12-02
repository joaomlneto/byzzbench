package byzzbench.simulator.protocols.fab2.messages;

import byzzbench.simulator.protocols.fab2.ProposalNumber;
import byzzbench.simulator.transport.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * <p>Message sent by proposers to indicate suspicion of the leader.</p>
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class SuspectMessage extends MessagePayload {
    private final String senderId;
    private final String suspectId;
    private final ProposalNumber proposalNumber;

    @Override
    public String getType() {
        return "SUSPECT";
    }

//    @Override
//    public long getRound() {
//        return 0;
//    }
//
//    @Override
//    public long getViewNumber() {
//        return proposalNumber.getViewNumber();
//    }
//
//    @Override
//    public long getSequenceNumber() { return 0; }
//
//    @Override
//    public byte[] getDigest() {
//        return suspectId.getBytes();
//    }
}

