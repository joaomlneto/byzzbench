package bftbench.runner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommitLog implements Serializable {
    private final List<Serializable> log = new ArrayList<>();

    public void append(Serializable operation) {
        log.add(operation);
    }
}
