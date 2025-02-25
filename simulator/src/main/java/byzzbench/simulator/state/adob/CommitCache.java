package byzzbench.simulator.state.adob;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * AdoB cache representing a commit.
 *
 * @see <a href="https://flint.cs.yale.edu/flint/publications/adob-tr.pdf">AdoB</a>
 */
@SuperBuilder
@Getter
public class CommitCache extends AdobCache {
    /**
     * The set of nodes that voted for this commit.
     */
    private final SortedSet<String> voters = new TreeSet<>();

    public CommitCache(long id, AdobCache parent) {
        super(id, parent);
    }

    public void addVoter(String voter) {
        voters.add(voter);
    }

    public void addVoters(Collection<String> voters) {
        this.voters.addAll(voters);
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
