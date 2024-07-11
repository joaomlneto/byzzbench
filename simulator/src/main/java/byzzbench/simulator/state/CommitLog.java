package byzzbench.simulator.state;

import byzzbench.simulator.Replica;
import java.io.Serializable;

/**
 * Abstract class for the commit log of a {@link Replica}.
 *
 * @param <T> The type of the entries in the commit log.
 */
public abstract class CommitLog<T extends Serializable>
    implements Serializable {
  public abstract void add(T operation);
}
