package byzzbench.simulator.state.adob;

import byzzbench.simulator.utils.NonNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * AdoB cache representing a local method invocation.
 *
 * @see <a href="https://flint.cs.yale.edu/flint/publications/adob-tr.pdf">AdoB</a>
 */
@SuperBuilder
public class MethodCache extends AdobCache {
    @Getter
    private final Serializable method;

    /**
     * The set of nodes that voted for this commit.
     */
    @Getter
    private final SortedSet<String> voters = new TreeSet<>();

    /**
     * The node that initiated the commit.
     */
    @NonNull
    private final String initiator;

    public MethodCache(long id, AdobCache parent, Serializable method, @NonNull String initialVoter) {
        super(id, parent);
        this.method = method;
        this.initiator = initialVoter;
        this.addVoter(initialVoter);
    }

    public void addVoter(String voter) {
        voters.add(voter);
    }

    @Override
    public String getCacheType() {
        return "Method";
    }

    @Override
    public byte getCRank() {
        return 2;
    }
}
