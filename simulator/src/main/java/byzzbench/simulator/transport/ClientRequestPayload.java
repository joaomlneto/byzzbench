package byzzbench.simulator.transport;

import java.io.Serializable;

public record ClientRequestPayload(String clientId, Serializable operation) implements Serializable {
}
