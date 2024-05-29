package byzzbench.runner.transport;

import java.util.List;

public abstract class MessageMutatorFactory {
    public abstract List<MessageMutator> mutators();
}
