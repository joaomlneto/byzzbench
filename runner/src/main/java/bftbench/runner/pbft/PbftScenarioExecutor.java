package bftbench.runner.pbft;

import bftbench.runner.Node;
import bftbench.runner.ScenarioExecutor;
import bftbench.runner.pbft.message.RequestMessage;
import bftbench.runner.pbft.mutator.PrePrepareMessageMutatorFactory;
import bftbench.runner.transport.Transport;
import lombok.extern.java.Log;

import java.util.Set;
import java.util.TreeSet;

@Log
public class PbftScenarioExecutor extends ScenarioExecutor {
    private final int NUM_NODES = 4;

    public PbftScenarioExecutor() {
        super(new Transport());
    }

    @Override
    public void setup() {
        try {
            Set<String> nodeIds = new TreeSet<>();
            for (int i = 0; i < NUM_NODES; i++) {
                nodeIds.add("p" + i);
            }

            for (int i = 0; i < NUM_NODES; i++) {
                String nodeId = "p" + i;
                MessageLog messageLog = new MessageLog(100, 100, 200);
                nodes.put(nodeId, new PbftNode<String, String>(nodeId, nodeIds, 1, 1000, messageLog, transport));
            }

            for (Node node : nodes.values()) {
                transport.addNode(node);
            }

            transport.registerMessageMutators(new PrePrepareMessageMutatorFactory<String>());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void run() {
        try {
            RequestMessage m = new RequestMessage("123", System.currentTimeMillis(), "c0");
            nodes.get("p1").handleMessage("c0", m);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
