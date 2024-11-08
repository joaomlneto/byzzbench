package byzzbench.simulator.protocols.pbft;

import byzzbench.simulator.BaseScenario;
import byzzbench.simulator.Replica;
import byzzbench.simulator.protocols.pbft_java.MessageLog;
import byzzbench.simulator.protocols.pbft_java.PbftTerminationPredicate;
import byzzbench.simulator.scheduler.Scheduler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.java.Log;

import java.util.SortedSet;
import java.util.TreeSet;

@Log
public class PbftScenario extends BaseScenario {
    private final int NUM_NODES = 4;
    private final PbftTerminationPredicate terminationCondition;

    public PbftScenario(Scheduler scheduler) {
        super("pbft", scheduler);
        this.terminationCondition = new PbftTerminationPredicate();
    }

    @Override
    public void loadScenarioParameters(JsonNode parameters) {
        // no parameters to load
    }

    @Override
    protected void setup() {
        try {
            SortedSet<String> nodeIds = new TreeSet<>();
            for (int i = 0; i < NUM_NODES; i++) {
                nodeIds.add(Character.toString((char) ('A' + i)));
            }

            // Create clients
            //this.setNumClients(1); // only works for the default client.
            this.addClient(new PbftClient("C1", this.transport));

            nodeIds.forEach(nodeId -> {
                MessageLog messageLog = new MessageLog(100, 100, 200);
                Replica replica = new PbftReplica(nodeId, nodeIds, transport, timekeeper);
                this.addNode(replica);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void run() {
        //getClients().values().forEach(Client::initializeClient);
    }
}
