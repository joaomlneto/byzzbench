package byzzbench.simulator.adob;

import lombok.Getter;

import java.util.Set;

public class TimeoutCache extends AdobCache {
    @Getter
    private final Set<String> voters;

    @Getter
    private final Set<String> supporters;

    public TimeoutCache(AdobCache parent, Set<String> voters, Set<String> supporters) {
        super(parent);
        this.voters = voters;
        this.supporters = supporters;
    }

    @Override
    public String getCacheType() {
        return "Timeout";
    }
}
