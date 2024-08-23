package byzzbench.simulator.state;

import byzzbench.simulator.Replica;

import java.io.Serializable;

/**
 * Abstract class for the commit log of a {@link Replica}
 */
public abstract class CommitLog implements Serializable {

    /**
     * Add an entry to the commit log.
     *
     * @param entry The entry to add.
     */
    public abstract void add(LogEntry entry);

    public abstract int getLength();

    public abstract LogEntry get(int index);
}
