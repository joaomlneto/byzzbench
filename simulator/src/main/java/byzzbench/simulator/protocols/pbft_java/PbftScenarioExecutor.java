package byzzbench.simulator.protocols.pbft_java;

import byzzbench.simulator.Replica;
import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.protocols.pbft_java.mutator.PrePrepareMessageMutatorFactory;
import byzzbench.simulator.transport.Transport;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

@Component
@Log
public class PbftScenarioExecutor<T extends Serializable> extends ScenarioExecutor<T> {
    private final int NUM_NODES = 4;

    public PbftScenarioExecutor() {
        super("pbft-java", new Transport());
        this.setNumClients(1);
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
                this.addNode(replica);
            });

            transport.registerMessageMutators(new PrePrepareMessageMutatorFactory());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void run() {
        // send a request message to node A
        try {
            this.transport.sendClientRequest("C0", "123", "A");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
