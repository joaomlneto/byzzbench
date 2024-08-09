package byzzbench.simulator.faults;

import byzzbench.simulator.transport.Event;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Interface for the behavior of a fault that can be applied to an {@link Event}.
 */
public interface FaultBehavior<T extends Event> extends Consumer<T> {
  /**
   * Returns the unique identifier of this fault behavior.
   * @return the unique identifier of this fault behavior
   */
  String getId();

  /**
   * Returns a collection of classes that this fault behavior can be applied to.
   * @return a collection of classes that this behavior can be applied to
   */
  Collection<Class<? extends Event>> getInputClasses();
}
