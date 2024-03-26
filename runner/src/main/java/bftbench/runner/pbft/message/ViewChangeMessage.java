package bftbench.runner.pbft.message;

import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

@Data
public class ViewChangeMessage implements Serializable {
    private final long newViewNumber;
    private final long lastSeqNumber;
    private final Collection<CheckpointMessage> checkpointProofs;
    private final Map<Long, Collection<IPhaseMessage>> preparedProofs;
    private final String replicaId;
}
