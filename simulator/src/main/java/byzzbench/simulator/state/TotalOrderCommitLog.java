package byzzbench.simulator.state;

import lombok.Getter;

import java.util.TreeMap;
import java.util.SortedMap;

/**
 * Commit log for total order replication, backed by a list.
 */
@Getter
public class TotalOrderCommitLog extends CommitLog {
    private final SortedMap<Long, LogEntry> log = new TreeMap<>();

    public void add(long sequenceNumber, LogEntry operation) {
        /* 
         * Might need to change for exception throwing,
         * as a same sequenceNumber cannot be in the log as
         * it would break safety!
         */
        log.putIfAbsent(sequenceNumber, operation);
    }

    @Override
    public int getLength() {
        return log.size();
    }

    @Override
    public LogEntry get(long index) {
        return log.get(index);
    }
}
