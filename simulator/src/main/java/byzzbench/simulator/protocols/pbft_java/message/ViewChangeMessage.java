package byzzbench.simulator.protocols.pbft_java.message;

import byzzbench.simulator.exploration_strategy.byzzfuzz.MessageWithByzzFuzzRoundInfo;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.util.Collection;
import java.util.SortedMap;


@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ViewChangeMessage extends MessagePayload implements MessageWithByzzFuzzRoundInfo {
    private final long newViewNumber;
    private final long lastSeqNumber;
    private final Collection<CheckpointMessage> checkpointProofs;
    private final SortedMap<Long, Collection<IPhaseMessage>> preparedProofs;
    private final String replicaId;

    @Override
    public String getType() {
        return "VIEW-CHANGE";
    }

    // ByzzFuzz round info
    @Override
    public long getViewNumber() {
        return newViewNumber;
    }

    @Override
    public long getRound() {
        // Use the last known committed sequence number for the round
        return lastSeqNumber;
    }
}
