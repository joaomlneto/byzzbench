package bftbench.runner.pbft.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReplyMessage<R> implements Serializable {
    private final long viewNumber;
    private final long timestamp;
    private final String clientId;
    private final String replicaId;
    private final R result;
}
