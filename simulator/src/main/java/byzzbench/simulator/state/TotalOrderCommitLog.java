package byzzbench.simulator.state;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Commit log for total order replication, backed by a list.
 */
@Getter
public class TotalOrderCommitLog extends CommitLog {
    private final List<LogEntry> log = new ArrayList<>();

    public void add(LogEntry operation) {
        log.add(operation);
    }

    @Override
    public int getLength() {
        return log.size();
    }

    @Override
    public LogEntry get(int index) {
        return log.get(index);
    }
}
