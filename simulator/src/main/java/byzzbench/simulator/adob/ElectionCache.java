package byzzbench.simulator.adob;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * AdoB cache representing a successful election.
 *
 * @see <a href="https://flint.cs.yale.edu/flint/publications/adob-tr.pdf">AdoB</a>
 */
@Getter
public class ElectionCache extends AdobCache {
    /**
     * The set of nodes that voted for this commit.
     */
    private final Set<String> voters = new HashSet<>();

    /**
     * The node that was elected leader.
     */
    private final String leader;

    /**
     * The node that initiated the commit.
     */
    private final String initiator;

    protected ElectionCache(AdobCache parent, String initialVoter, String leader) {
        super(parent);
        this.initiator = initialVoter;
        this.addVoter(initialVoter);
        this.leader = leader;
    }

    public void addVoter(String voter) {
        voters.add(voter);
    }

    @Override
    public String getCacheType() {
        return "Election";
    }
}
