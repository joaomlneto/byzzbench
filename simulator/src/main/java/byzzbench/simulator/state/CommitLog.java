package byzzbench.simulator.state;

import java.io.Serializable;

import byzzbench.simulator.Replica;

/**
 * Abstract class for the commit log of a {@link Replica}
 */
public abstract class CommitLog implements Serializable {

    /**
     * Add an entry to the commit log.
     *
     * @param entry The entry to add.
     */
    public abstract void add(long sequencNumber, LogEntry entry);

    public abstract int getLength();

    public abstract LogEntry get(long index);
}
