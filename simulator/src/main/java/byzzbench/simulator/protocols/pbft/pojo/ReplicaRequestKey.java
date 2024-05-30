package byzzbench.simulator.protocols.pbft.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReplicaRequestKey implements Serializable {
    private final String clientId;
    private final long timestamp;
}
