package byzzbench.runner.transport;

import java.io.Serializable;

public interface MessagePayload extends Serializable {
    String getType();
}
