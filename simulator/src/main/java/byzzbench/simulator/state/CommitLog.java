package byzzbench.simulator.state;

import java.io.Serializable;

import byzzbench.simulator.Replica;

/**
 * Abstract class for the commit log of a {@link Replica}
 */
public abstract class CommitLog implements Serializable {

    /**
     * Add an entry to the commit log at a given sequence number.
     *
     * @param sequenceNumber The sequence number of the entry.
     * @param entry          The entry to add.
     */
    public abstract void add(long sequenceNumber, LogEntry entry);

    /**
     * Appends an entry to the commit log at lowest sequence number that
     * is higher than any other sequence number in the log.
     *
     * @param entry The entry to add.
     */
    public abstract void add(long sequencNumber, LogEntry entry);

    /**
     * Get the number of entries in the commit log.
     *
     * @return the number of entries in the commit log.
     */
    public abstract int getLength();

    /**
     * Get the entry with a given sequence number.
     *
     * @param sequenceNumber The sequence number of the entry.
     * @return The entry with the given sequence number.
     */
    public abstract LogEntry get(long sequenceNumber);

    /**
     * Checks if the commit log is empty.
     *
     * @return True if the commit log is empty, false otherwise.
     */
    public abstract boolean isEmpty();
}
