package byzzbench.simulator.state.adore;

import java.util.Set;
import lombok.Getter;

/**
 * AdoB cache representing a timeout.
 *
 * @see <a
 *     href="https://flint.cs.yale.edu/flint/publications/adob-tr.pdf">AdoB</a>
 */
public class TimeoutCache extends AdoreCache {
  @Getter private final Set<String> voters;

  @Getter private final Set<String> supporters;

  public TimeoutCache(long id, AdoreCache parent, Set<String> voters,
                      Set<String> supporters) {
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
