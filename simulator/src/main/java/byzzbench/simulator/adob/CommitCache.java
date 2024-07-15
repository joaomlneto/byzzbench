package byzzbench.simulator.adob;

import lombok.Getter;

import java.util.Set;

@Getter
public class CommitCache extends AdobCache {
    private final Set<String> voters;

    public CommitCache(AdobCache parent, Set<String> voters) {
        super(parent);
        this.voters = voters;
    }

    @Override
    public String getCacheType() {
        return "Commit";
    }
}
