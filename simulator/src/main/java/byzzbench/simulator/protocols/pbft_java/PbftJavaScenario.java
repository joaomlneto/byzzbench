package byzzbench.simulator.protocols.pbft_java;

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
    private final PbftTerminationCondition terminationCondition;
    private final int numReplicas = 4;

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
            for (int i = 0; i < numReplicas; i++) {
                nodeIds.add(Character.toString((char) ('A' + i)));
            }

            nodeIds.forEach(nodeId -> {
                MessageLog messageLog = new MessageLog(100, 100, 200);
                Replica replica = new PbftJavaReplica<String, String>(nodeId, nodeIds, 1, 1000, messageLog, timekeeper, transport);
                this.addNode(replica);
            });

            this.setNumClients(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void run() {
        // nothing to do
    }

    @Override
    public TerminationCondition getTerminationCondition() {
        return this.terminationCondition;
    }
}
