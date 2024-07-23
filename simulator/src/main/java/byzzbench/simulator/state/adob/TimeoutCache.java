package byzzbench.simulator.state.adob;

import lombok.Getter;

import java.util.Set;

/**
 * AdoB cache representing a timeout.
 *
 * @see <a href="https://flint.cs.yale.edu/flint/publications/adob-tr.pdf">AdoB</a>
 */
public class TimeoutCache extends AdobCache {
    @Getter
    private final Set<String> voters;

    @Getter
    private final Set<String> supporters;

    public TimeoutCache(long id, AdobCache parent, Set<String> voters, Set<String> supporters) {
        super(id, parent);
        this.voters = voters;
        this.supporters = supporters;
    }

    @Override
    public String getCacheType() {
        return "Timeout";
    }

    @Override
    public byte getCRank() {
        return 4;
    }
}
