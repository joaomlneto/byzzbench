package byzzbench.simulator.state.adore;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;

/**
 * AdoB cache representing a successful election.
 *
 * @see <a
 *     href="https://flint.cs.yale.edu/flint/publications/adob-tr.pdf">AdoB</a>
 */
@Getter
public class ElectionCache extends AdoreCache {
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

  protected ElectionCache(long id, AdoreCache parent, String initialVoter,
                          String leader) {
    super(id, parent);
    this.initiator = initialVoter;
    this.addVoter(initialVoter);
    this.leader = leader;
  }

  public void addVoter(String voter) { voters.add(voter); }

  @Override
  public String getCacheType() {
    return "Election";
  }

  @Override
  public byte getCRank() {
    return 1;
  }
}
