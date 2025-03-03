package byzzbench.simulator.protocols.fab2.messages;

import byzzbench.simulator.protocols.pbft_java.message.IPhaseMessage;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.*;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class NewViewChangeMessage extends MessagePayload {
    @Getter
    private final long proposalNumber;
    private final String newLeaderId;

    @Override
    public String getType() {
        return "NEW_VIEW";
    }

//    @Override
//    public long getViewNumber() {
//        return proposalNumber;
//    }
//
//
//    @Override
//    public long getSequenceNumber() {
//        return 0;
//    }
//
//    @Override
//    public long getRound() {
//        return 0;
//    }
//
//    @Override
//    public byte[] getDigest() {
//        return new byte[0];
//    }
}
