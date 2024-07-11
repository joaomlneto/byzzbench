package byzzbench.simulator.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * Commit log for total order replication, backed by a list.
 *
 * @param <T> The type of the entries in the commit log.
 */
@Getter
public class TotalOrderCommitLog<T extends Serializable> extends CommitLog<T> {
  private final List<T> log = new ArrayList<>();

  public void add(T operation) { log.add(operation); }
}
