package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.scheduler.Scheduler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.java.Log;

import java.util.SortedSet;
import java.util.TreeSet;

@Log
public class TendermintScenarioExecutor extends Scenario {
    private final int NUM_NODES = 4;

    public TendermintScenarioExecutor(Scheduler scheduler) {
        super("tendermint", scheduler);
        this.terminationCondition = new TendermintTerminationPredicate();
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
                Replica replica = new TendermintReplica(nodeId, nodeIds, this);
                this.addNode(replica);
            });

            this.setNumClients(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void run() {
//        // send a request message to node A
//        try {
//            this.setNumClients(1);
//            this.transport.sendClientRequest("C0", "123", "A");
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public Replica cloneReplica(Replica replica) {
        return new TendermintReplica(replica.getId(), replica.getNodeIds(), this);
    }

    @Override
    public int maxFaultyReplicas(int n) {
        return 1;
    }
}
