package byzzbench.simulator;

import byzzbench.simulator.scheduler.BaseScheduler;
import byzzbench.simulator.scheduler.RandomScheduler;
import byzzbench.simulator.transport.Transport;
import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class for running a scenario, which consists of a set of {@link Replica} and a {@link Transport} layer.
 *
 * @param <T> The type of the entries in the commit log of each {@link Replica}.
 */
@Getter
public abstract class ScenarioExecutor<T extends Serializable> {
    protected final Transport<T> transport;

    protected final BaseScheduler<T> scheduler;

    protected final Map<String, Replica<T>> nodes = new HashMap<>();

    public ScenarioExecutor(Transport<T> transport) {
        this.transport = transport;
        this.scheduler = new RandomScheduler<>(transport);
        this.setup();
    }

    public void reset() {
        this.transport.reset();
        this.nodes.clear();
        this.setup();
        for (Replica<T> r : this.nodes.values()) {
            r.initialize();
        }
    }

    public void addNode(Replica<T> replica) {
        this.nodes.put(replica.getNodeId(), replica);
    }

    public abstract void setup();

    public abstract void run();
}
