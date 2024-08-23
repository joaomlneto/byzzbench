package byzzbench.simulator.state;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * Commit log for total order replication, backed by a list.
 */
@Getter
public class TotalOrderCommitLog extends CommitLog {
  private final List<LogEntry> log = new ArrayList<>();

  public void add(LogEntry operation) { log.add(operation); }

  @Override
  public int getLength() {
    return log.size();
  }

  @Override
  public LogEntry get(int index) {
    return log.get(index);
  }
}
