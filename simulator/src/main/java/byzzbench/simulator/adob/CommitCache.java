package byzzbench.simulator.adob;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * AdoB cache representing a commit.
 *
 * @see <a href="https://flint.cs.yale.edu/flint/publications/adob-tr.pdf">AdoB</a>
 */
@Getter
public class CommitCache extends AdobCache {
    /**
     * The set of nodes that voted for this commit.
     */
    private final Set<String> voters = new HashSet<>();

    /**
     * The node that initiated the commit.
     */
    private final String initiator;

    public CommitCache(long id, AdobCache parent, String initialVoter) {
        super(id, parent);
        this.initiator = initialVoter;
        this.addVoter(initialVoter);
    }

    public void addVoter(String voter) {
        voters.add(voter);
    }

    @Override
    public String getCacheType() {
        return "Commit";
    }

    @Override
    public byte getCRank() {
        return 3;
    }
}
