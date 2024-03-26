package bftbench.runner.pbft.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class CheckpointMessage implements Serializable {
    private final long lastSeqNumber;
    private final byte[] digest;
    private final String replicaId;
}
