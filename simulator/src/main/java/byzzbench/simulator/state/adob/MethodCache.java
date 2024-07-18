package byzzbench.simulator.state.adob;

import lombok.Getter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


/**
 * AdoB cache representing a local method invocation.
 *
 * @see <a href="https://flint.cs.yale.edu/flint/publications/adob-tr.pdf">AdoB</a>
 */
public class MethodCache extends AdobCache {
    @Getter
    private final Serializable method;

    /**
     * The set of nodes that voted for this commit.
     */
    @Getter
    private final Set<String> voters = new HashSet<>();

    /**
     * The node that initiated the commit.
     */
    private final String initiator;

    public MethodCache(long id, AdobCache parent, Serializable method, String initialVoter) {
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
