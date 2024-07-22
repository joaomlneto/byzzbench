package byzzbench.simulator.state.adore;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;

/**
 * AdoB cache representing a commit.
 *
 * @see <a
 *     href="https://flint.cs.yale.edu/flint/publications/adob-tr.pdf">AdoB</a>
 */
@Getter
public class CommitCache extends AdoreCache {
  /**
   * The set of nodes that voted for this commit.
   */
  private final Set<String> voters = new HashSet<>();

  public CommitCache(long id, AdoreCache parent) { super(id, parent); }

  public void addVoter(String voter) { voters.add(voter); }

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
