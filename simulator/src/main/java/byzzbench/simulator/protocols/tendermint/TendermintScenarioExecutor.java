package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.Client;
import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.protocols.pbft_java.PbftClient;
import lombok.extern.java.Log;

import java.util.SortedSet;
import java.util.TreeSet;

@Log
public class TendermintScenarioExecutor extends Scenario {
    private static final String SCENARIO_ID = "tendermint";
    private final int NUM_NODES = 4;

    public TendermintScenarioExecutor(Schedule schedule) {
        super(schedule, SCENARIO_ID);
        this.terminationCondition = new TendermintTerminationPredicate();
    }

    @Override
    public void loadScenarioParameters(ScenarioParameters parameters) {
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

            // create a single client?
            String clientId = "C0";
            Client client = new PbftClient(this, clientId);
            this.addClient(client);
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

    @Override
    public Class<? extends Replica> getReplicaClass() {
        return TendermintReplica.class;
    }

    @Override
    public Class<? extends Client> getClientClass() {
        return PbftClient.class;
    }
}
