package byzzbench.simulator.transport;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Abstract class for mutating {@link MessageEvent}.
 * This class is used as a base to introduce arbitrary faults in the simulation.
 */
@Getter
@RequiredArgsConstructor
@ToString
public abstract class MessageMutator
    implements Serializable, Function<Serializable, Serializable> {
  private final String name;

  private final Collection<Class<? extends Serializable>> inputClasses;
}
