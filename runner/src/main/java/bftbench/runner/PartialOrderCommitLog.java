package bftbench.runner;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PartialOrderCommitLog<T extends Serializable, Comparable> extends CommitLog<T> {
    private final List<LogEntry<T>> log = new ArrayList<>();

    public void append(T operation, T parent) {
        log.add(new LogEntry<>(operation, parent));
    }

    @Data
    public class LogEntry<T> {
        private final T operation;
        private final T parent;

        public LogEntry(T operation, T parent) {
            this.operation = operation;
            this.parent = parent;
        }
    }
}
