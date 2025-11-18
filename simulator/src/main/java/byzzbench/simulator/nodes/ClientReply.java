package byzzbench.simulator.nodes;

import java.io.Serializable;

public interface ClientReply {
    Serializable getRequestId();

    Serializable getReply();
}
