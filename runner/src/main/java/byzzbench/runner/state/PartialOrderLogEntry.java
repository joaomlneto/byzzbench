package byzzbench.runner.state;

import java.io.Serializable;

public interface PartialOrderLogEntry<K> extends Serializable {
    K getParentHash();
}
