package byzzbench.simulator.faults.factories;

import byzzbench.simulator.faults.faults.MessageMutationFault;

import java.util.List;

/**
 * Abstract class for creating a list of {@link MessageMutationFault} instances.
 */
public abstract class MessageMutatorFactory {
    public abstract List<MessageMutationFault> mutators();
}
