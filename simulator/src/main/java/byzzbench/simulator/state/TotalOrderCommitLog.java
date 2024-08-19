package byzzbench.simulator.state;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Commit log for total order replication, backed by a list.
 *
 * @param <T> The type of the entries in the commit log.
 */
@Getter
public class TotalOrderCommitLog<T extends Serializable> extends CommitLog<T> {
  private final List<T> log = new ArrayList<>();

  public void add(T operation) { log.add(operation); }

  @Override
  public int getLength() {
    return log.size();
  }

    @Override
    public T get(int index) {
        return log.get(index);
    }
}
