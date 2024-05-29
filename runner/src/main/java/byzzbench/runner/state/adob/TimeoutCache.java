package byzzbench.runner.state.adob;

import lombok.Data;

import java.util.Set;

@Data
public class TimeoutCache implements AdobCache {
    private final AdobCache parent;
    private final Set<String> voters;
    private final Set<String> supporters;
    private final long timestamp;
}
