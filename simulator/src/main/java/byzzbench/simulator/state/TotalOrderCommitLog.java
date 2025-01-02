package byzzbench.simulator.state;

import lombok.Getter;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Commit log for total order replication, backed by a list.
 */
@Getter
public class TotalOrderCommitLog extends CommitLog {
    /**
     * The initial sequence number if the log is empty and a new entry is added
     * without specifying a sequence number.
     */
    public static final long INITIAL_SEQUENCE_NUMBER = 0;

    /**
     * The log of entries, indexed by their sequence number.
     */
    private final SortedMap<Long, LogEntry> entries = new TreeMap<>();

    /**
     * The highest sequence number that has been committed.
     */
    private long highestCommittedSequenceNumber = INITIAL_SEQUENCE_NUMBER - 1;

    @Override
    public synchronized void add(long sequenceNumber, LogEntry operation) {
        // Check if the sequence number already exists in the log.
        if (entries.containsKey(sequenceNumber)) {
            throw new IllegalArgumentException(String.format("Sequence number %d already exists in the log", sequenceNumber));
        }

        entries.putIfAbsent(sequenceNumber, operation);
        highestCommittedSequenceNumber = Math.max(highestCommittedSequenceNumber, sequenceNumber);
    }

    @Override
    public synchronized void add(LogEntry operation) {
        long sequenceNumber = ++highestCommittedSequenceNumber;
        add(sequenceNumber, operation);
    }

    @Override
    public synchronized int getLength() {
        return entries.size();
    }

    @Override
    public synchronized LogEntry get(long sequenceNumber) {
        return entries.get(sequenceNumber);
    }

    @Override
    public synchronized boolean isEmpty() {
        return entries.isEmpty();
    }
}
