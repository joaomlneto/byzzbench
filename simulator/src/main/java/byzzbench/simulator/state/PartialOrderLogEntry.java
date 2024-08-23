package byzzbench.simulator.state;

public interface PartialOrderLogEntry<K> extends LogEntry {
  K getParentHash();
}
