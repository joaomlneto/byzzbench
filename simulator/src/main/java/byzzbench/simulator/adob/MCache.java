package byzzbench.simulator.adob;

import lombok.Getter;

import java.io.Serializable;
import java.util.Set;

public class MCache extends AdobCache {
    @Getter
    private final Serializable method;

    @Getter
    private final Set<String> voters;

    public MCache(AdobCache parent, Serializable method, Set<String> voters) {
        super(parent);
        this.method = method;
        this.voters = voters;
    }

    @Override
    public String getCacheType() {
        return "M";
    }
}
