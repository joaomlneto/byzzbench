package bftbench.runner.pbft.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class RequestMessage<O> implements Serializable {
    private final O operation;
    private final long timestamp;
    private final String clientId;
}
