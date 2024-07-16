package byzzbench.simulator.adob;

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

    public TimeoutCache(AdobCache parent, Set<String> voters, Set<String> supporters) {
        super(parent);
        this.voters = voters;
        this.supporters = supporters;
    }

    @Override
    public String getCacheType() {
        return "Timeout";
    }
}
