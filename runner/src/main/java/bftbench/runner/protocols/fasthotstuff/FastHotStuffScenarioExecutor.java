package bftbench.runner.protocols.fasthotstuff;

import bftbench.runner.Replica;
import bftbench.runner.ScenarioExecutor;
import bftbench.runner.transport.Transport;
import lombok.extern.java.Log;

import java.util.Set;
import java.util.TreeSet;

@Log
public class FastHotStuffScenarioExecutor extends ScenarioExecutor {
    private final int NUM_NODES = 4;

    public FastHotStuffScenarioExecutor() {
        super(new Transport());
    }

    @Override
    public void setup() {
        try {
            Set<String> nodeIds = new TreeSet<>();
            for (int i = 0; i < NUM_NODES; i++) {
                nodeIds.add(Character.toString((char) ('A' + i)));
            }

            nodeIds.forEach(nodeId -> {
                Replica replica = new FastHotStuffReplica(nodeId, nodeIds, transport);
                nodes.put(nodeId, replica);
                transport.addNode(replica);
            });

            log.info("Nodes: " + nodes);

            //transport.registerMessageMutators(new PrePrepareMessageMutatorFactory());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void run() {
    }
}
