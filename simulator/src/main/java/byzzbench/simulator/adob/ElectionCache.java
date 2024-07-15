package byzzbench.simulator.adob;

import java.util.Set;

public class ElectionCache extends AdobCache {
    private final Set<String> voters;
    private final String leader;

    protected ElectionCache(AdobCache parent, Set<String> voters, String leader) {
        super(parent);
        this.voters = voters;
        this.leader = leader;
    }
}
