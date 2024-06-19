package byzzbench.simulator.adob;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class MCache implements AdobCache {
    private final AdobCache parent;
    private final Serializable method;
    private final Set<String> voters;
    private final long timestamp;
}
