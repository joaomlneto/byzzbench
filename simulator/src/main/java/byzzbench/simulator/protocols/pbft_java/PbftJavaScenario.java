package byzzbench.simulator.protocols.pbft_java;

import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.scheduler.Scheduler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.java.Log;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A scenario for running the PBFT-Java protocol, an implementation of the PBFT protocol in Java:
 * https://github.com/caojohnny/pbft-java
 */
@Log
public class PbftJavaScenario extends Scenario {
    private final int numReplicas = 4;

    public PbftJavaScenario(Scheduler scheduler) {
        super("pbft-java", scheduler);
        this.terminationCondition = new PbftTerminationPredicate();
    }

    @Override
    public void loadScenarioParameters(JsonNode parameters) {
        // no parameters to load
    }

    @Override
    protected void setup() {
        SortedSet<String> nodeIds = new TreeSet<>();
        for (int i = 0; i < numReplicas; i++) {
            nodeIds.add(Character.toString((char) ('A' + i)));
        }

        nodeIds.forEach(nodeId -> {
            MessageLog messageLog = new MessageLog(100, 100, 200);
            Replica replica = new PbftJavaReplica<String, String>(nodeId, this, 1, 1000, messageLog);
            this.addNode(replica);
        });

        this.setNumClients(1);
    }

    @Override
    public synchronized void run() {
        // nothing to do
    }

    @Override
    public Replica cloneReplica(Replica replica) {
        MessageLog messageLog = new MessageLog(100, 100, 200);
        return new PbftJavaReplica<String, String>(replica.getId(), this, 1, 1000, messageLog);
    }

    @Override
    public int maxFaultyReplicas(int n) {
        return (n - 1) / 3;
    }
}
