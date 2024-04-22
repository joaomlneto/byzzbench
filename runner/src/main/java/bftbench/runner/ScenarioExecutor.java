package bftbench.runner;

import bftbench.runner.transport.Transport;
import lombok.Getter;

import java.util.Map;

public abstract class ScenarioExecutor {
    @Getter
    protected final Transport transport;

    @Getter
    protected final Map<String, Node> nodes = new java.util.HashMap<>();

    public ScenarioExecutor(Transport transport) {
        this.transport = transport;
        this.setup();
    }

    public void reset() {
        this.transport.reset();
        this.nodes.clear();
        this.setup();
    }

    public void addNode(Node node) {
        this.nodes.put(node.getNodeId(), node);
    }


    public abstract void setup();
    public abstract void run();
}
