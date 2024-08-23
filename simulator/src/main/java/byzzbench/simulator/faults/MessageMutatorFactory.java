package byzzbench.simulator.faults;

import java.util.List;

/**
 * Abstract class for creating a list of {@link MessageMutationFault} instances.
 */
public abstract class MessageMutatorFactory {
  public abstract List<MessageMutationFault> mutators();
}
