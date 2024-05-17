package bftbench.runner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommitLog<T extends Serializable> implements Serializable {
    private final List<T> log = new ArrayList<>();

    public void append(T operation) {
        log.add(operation);
    }
}
