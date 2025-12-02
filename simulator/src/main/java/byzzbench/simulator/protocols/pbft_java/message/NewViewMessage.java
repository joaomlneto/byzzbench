package byzzbench.simulator.protocols.pbft_java.message;

import byzzbench.simulator.exploration_strategy.byzzfuzz.MessageWithByzzFuzzRoundInfo;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.util.Collection;
import java.util.Comparator;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class NewViewMessage extends MessagePayload implements MessageWithByzzFuzzRoundInfo {
    private final long newViewNumber;
    private final Collection<ViewChangeMessage> viewChangeProofs;
    private final Collection<PrePrepareMessage> preparedProofs;

    @Override
    public String getType() {
        return "NEW-VIEW";
    }

    // ByzzFuzz round info
    @Override
    public long getViewNumber() {
        return newViewNumber;
    }

    @Override
    public long getRound() {
        // Use the highest prepared sequence as the round, if available
        if (preparedProofs == null || preparedProofs.isEmpty()) {
            return 0;
        }
        return preparedProofs.stream()
                .map(PrePrepareMessage::getSequenceNumber)
                .max(Comparator.naturalOrder())
                .orElse(0L);
    }
}
