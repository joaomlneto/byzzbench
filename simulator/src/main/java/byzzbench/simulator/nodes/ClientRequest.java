package byzzbench.simulator.nodes;

import java.io.Serializable;

public interface ClientRequest extends Serializable {
    Serializable getRequestId();

    Serializable getOperation();
}
