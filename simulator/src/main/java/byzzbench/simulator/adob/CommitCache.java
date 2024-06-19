package byzzbench.simulator.adob;

import lombok.Data;

import java.util.Set;

@Data
public class CommitCache implements AdobCache {
    private final AdobCache parent;
    private final Set<String> voters;
    private final long timestamp;

}
