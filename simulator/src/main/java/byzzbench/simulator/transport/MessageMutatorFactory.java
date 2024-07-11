package byzzbench.simulator.transport;

import java.util.List;

/**
 * Abstract class for creating a list of {@link MessageMutator} instances.
 */
public abstract class MessageMutatorFactory {
    public abstract List<MessageMutator> mutators();
}
