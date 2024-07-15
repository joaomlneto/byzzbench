package byzzbench.simulator.adob;

import lombok.Getter;

import java.util.Set;

@Getter
public class ElectionCache extends AdobCache {
    private final Set<String> voters;
    private final String leader;

    protected ElectionCache(AdobCache parent, Set<String> voters, String leader) {
        super(parent);
        this.voters = voters;
        this.leader = leader;
    }

    @Override
    public String getCacheType() {
        return "Election";
    }
}
