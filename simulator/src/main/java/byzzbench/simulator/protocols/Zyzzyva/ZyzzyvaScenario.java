package byzzbench.simulator.protocols.Zyzzyva;

import byzzbench.simulator.BaseScenario;
import byzzbench.simulator.Replica;
import byzzbench.simulator.scheduler.Scheduler;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.SortedSet;
import java.util.TreeSet;

public class ZyzzyvaScenario extends BaseScenario {
    private final int numReplicas = 4;
    private final int numClients = 2;

    public ZyzzyvaScenario(Scheduler scheduler) {
        super("zyzzyva", scheduler);
    }

    @Override
    protected void loadScenarioParameters(JsonNode parameters) {

    }

    @Override
    protected void setup() {
        SortedSet<String> replicaIds = new TreeSet<>();
        for (int i = 0; i < numReplicas; i++) {
            replicaIds.add(Character.valueOf((char) ('A' + i)).toString());
        }

        for (String replicaId : replicaIds) {
            ZyzzyvaReplica replica = new ZyzzyvaReplica(replicaId, replicaIds, this, 15);
            this.addNode(replica);
        }

        for (int i = 0; i < numClients; i++) {
            ZyzzyvaClient client = new ZyzzyvaClient(this, "Client " + i);
            this.addClient(client);
        }

    }

    @Override
    protected void run() {

    }

    @Override
    public int maxFaultyReplicas() {
        return maxFaultyReplicas(numReplicas);
    }

    @Override
    public int maxFaultyReplicas(int n) {
        return (n - 1) / 3;
    }

    @Override
    public Replica cloneReplica(Replica replica) {
        return new ZyzzyvaReplica(replica.getId(), replica.getNodeIds(), this, 15);
    }
}
