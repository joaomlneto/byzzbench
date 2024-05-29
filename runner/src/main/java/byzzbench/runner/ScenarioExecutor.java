package byzzbench.runner;

import byzzbench.runner.transport.Transport;
import lombok.Getter;

import java.io.Serializable;
import java.util.Map;

public abstract class ScenarioExecutor<T extends Serializable> {
    @Getter
    protected final Transport<T> transport;

    @Getter
    protected final Map<String, Replica<T>> nodes = new java.util.HashMap<>();

    public ScenarioExecutor(Transport<T> transport) {
        this.transport = transport;
        this.setup();
    }

    public void reset() {
        this.transport.reset();
        this.nodes.clear();
        this.setup();
    }

    public void addNode(Replica<T> replica) {
        this.nodes.put(replica.getNodeId(), replica);
    }


    public abstract void setup();

    public abstract void run();
}
