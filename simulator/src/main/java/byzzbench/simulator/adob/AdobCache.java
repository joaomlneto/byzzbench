package byzzbench.simulator.adob;

import java.io.Serializable;

public interface AdobCache extends Serializable {
    AdobCache getParent();

    long getTimestamp();
}
