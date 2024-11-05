package byzzbench.simulator.protocols.hbft;

import byzzbench.simulator.BaseScenario;
import byzzbench.simulator.Replica;
import byzzbench.simulator.TerminationCondition;
import byzzbench.simulator.scheduler.Scheduler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.java.Log;

import java.util.SortedSet;
import java.util.TreeSet;

@Log
public class PbftJavaScenario extends BaseScenario {
    private final int NUM_NODES = 4;
    private final PbftTerminationCondition terminationCondition;

    public PbftJavaScenario(Scheduler scheduler) {
        super("pbft-java", scheduler);
        this.terminationCondition = new PbftTerminationCondition();
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

            nodeIds.forEach(nodeId -> {
                MessageLog messageLog = new MessageLog(100, 100, 200);
                Replica replica = new HbftJavaReplica<String, String>(nodeId, nodeIds, 1, 1000, messageLog, transport);
                this.addNode(replica);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void run() {
        // send a request message to node A
        try {
            this.setNumClients(1);
            this.transport.sendClientRequest("C0", "123", "A");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public TerminationCondition getTerminationCondition() {
        return this.terminationCondition;
    }
}
