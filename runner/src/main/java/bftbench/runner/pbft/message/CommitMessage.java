package bftbench.runner.pbft.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommitMessage implements Serializable, IPhaseMessage {
    private final long viewNumber;
    private final long sequenceNumber;
    private final byte[] digest;
    private final String replicaId;
}
