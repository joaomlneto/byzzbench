package byzzbench.simulator.state.adob;

import java.util.SortedSet;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * AdoB cache representing a timeout.
 *
 * @see <a
 *     href="https://flint.cs.yale.edu/flint/publications/adob-tr.pdf">AdoB</a>
 */
@SuperBuilder
@Getter
public class TimeoutCache extends AdobCache {
  private final SortedSet<String> voters;
  private final SortedSet<String> supporters;

  public TimeoutCache(long id, AdobCache parent, SortedSet<String> voters,
                      SortedSet<String> supporters) {
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
