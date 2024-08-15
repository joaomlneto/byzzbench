package byzzbench.simulator.faults;

import java.io.Serializable;
import java.util.List;

/**
 * Abstract class for creating a list of {@link MessageMutationFault} instances.
 */
public abstract class MessageMutatorFactory<T extends Serializable> {
  public abstract List<MessageMutationFault<T>> mutators();
}
