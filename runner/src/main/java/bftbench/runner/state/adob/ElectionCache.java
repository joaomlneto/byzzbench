package bftbench.runner.state.adob;

import lombok.Data;

import java.util.Set;

@Data
public class ElectionCache implements AdobCache {
    private final AdobCache parent;
    private final Set<String> voters;
    private final String leader;
    private final long timestamp;
}
