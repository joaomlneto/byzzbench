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

  /**
   * Add an operation to the commit log.
   *
   * @param operation The operation to add.
   */
  public abstract void add(T operation);

  public abstract int getLength();

  public abstract T get(int index);
}
