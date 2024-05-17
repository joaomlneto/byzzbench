package bftbench.runner.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TotalOrderCommitLog<T extends Serializable> extends CommitLog<T> {
    private final List<T> log = new ArrayList<>();

    public void add(T operation) {
        log.add(operation);
    }
}
