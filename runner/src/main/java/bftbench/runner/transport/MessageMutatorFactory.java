package bftbench.runner.transport;

import java.io.Serializable;
import java.util.List;

public abstract class MessageMutatorFactory<T extends Serializable> {
    public abstract List<MessageMutator<T>> mutators();
}
