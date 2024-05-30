package byzzbench.simulator.transport;

import java.io.Serializable;

public interface MessagePayload extends Serializable {
    String getType();
}
