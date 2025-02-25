package byzzbench.simulator.state.adob;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * AdoB cache representing a successful election.
 *
 * @see <a href="https://flint.cs.yale.edu/flint/publications/adob-tr.pdf">AdoB</a>
 */
@SuperBuilder
@Getter
public class ElectionCache extends AdobCache {
    /**
     * The set of nodes that voted for this commit.
     */
    private final SortedSet<String> voters = new TreeSet<>();

    /**
     * The node that was elected leader.
     */
    private final String leader;

    /**
     * The node that initiated the commit.
     */
    private final String initiator;

    protected ElectionCache(long id, AdobCache parent, String initialVoter, String leader) {
        super(id, parent);
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

    @Override
    public byte getCRank() {
        return 1;
    }
}
