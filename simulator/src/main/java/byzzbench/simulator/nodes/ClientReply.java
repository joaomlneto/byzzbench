package byzzbench.simulator.nodes;

import java.io.Serializable;

public interface ClientReply extends Serializable {
    Serializable getRequestId();

    Serializable getReply();
}
