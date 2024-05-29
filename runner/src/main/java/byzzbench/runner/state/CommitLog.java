package byzzbench.runner.state;

import java.io.Serializable;

public abstract class CommitLog<T extends Serializable> implements Serializable {
    public abstract void add(T operation);
}
