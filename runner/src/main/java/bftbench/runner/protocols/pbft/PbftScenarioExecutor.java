package bftbench.runner.protocols.pbft;

import bftbench.runner.Replica;
import bftbench.runner.ScenarioExecutor;
import bftbench.runner.protocols.pbft.message.RequestMessage;
import bftbench.runner.protocols.pbft.mutator.PrePrepareMessageMutatorFactory;
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
                nodeIds.add(Character.toString((char) ('A' + i)));
            }

            nodeIds.forEach(nodeId -> {
                MessageLog messageLog = new MessageLog(100, 100, 200);
                Replica replica = new PbftReplica<String, String>(nodeId, nodeIds, 1, 1000, messageLog, transport);
                nodes.put(nodeId, replica);
                transport.addNode(replica);
            });

            transport.registerMessageMutators(new PrePrepareMessageMutatorFactory());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void run() {
        try {
            RequestMessage m = new RequestMessage("123", System.currentTimeMillis(), "c0");
            nodes.get("A").handleMessage("c0", m);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
