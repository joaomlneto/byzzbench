package bftbench.runner;

import bftbench.runner.transport.Transport;
import lombok.Getter;

import java.util.Map;

public abstract class ScenarioExecutor {
    @Getter
    protected final Transport transport;

    @Getter
    protected final Map<String, Replica> nodes = new java.util.HashMap<>();

    public ScenarioExecutor(Transport transport) {
        this.transport = transport;
        this.setup();
    }

    public void reset() {
        this.transport.reset();
        this.nodes.clear();
        this.setup();
    }

    public void addNode(Replica replica) {
        this.nodes.put(replica.getNodeId(), replica);
    }


    public abstract void setup();

    public abstract void run();
}
