package byzzbench.simulator.nodes;

import java.io.Serializable;
import java.time.Instant;

public interface ClientRequest extends Serializable {
    Serializable getRequestId();

    Serializable getOperation();

    Instant getTimestamp();
}
