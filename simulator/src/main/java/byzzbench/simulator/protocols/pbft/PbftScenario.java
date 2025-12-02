package byzzbench.simulator.protocols.pbft;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.nodes.Client;
import byzzbench.simulator.nodes.Replica;
import byzzbench.simulator.protocols.pbft_java.MessageLog;
import lombok.extern.java.Log;

import java.util.SortedSet;
import java.util.TreeSet;

@Log
public class PbftScenario extends Scenario {
    private final int NUM_NODES = 4;

    /**
     * Creates a new scenario from the given schedule.
     *
     * @param schedule The schedule for the scenario.
     */
    public PbftScenario(Schedule schedule) {
        super(schedule);
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

            // Create clients
            //this.setNumClients(1); // only works for the default client.
            this.addClient(new PbftClient(this, "C1"));

            nodeIds.forEach(nodeId -> {
                MessageLog messageLog = new MessageLog(100, 100, 200);
                Replica replica = new PbftReplica(nodeId, this);
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

    @Override
    public int maxFaultyReplicas(int n) {
        return (n - 1) / 3;
    }

    @Override
    public Class<? extends Replica> getReplicaClass() {
        return PbftReplica.class;
    }

    @Override
    public Class<? extends Client> getClientClass() {
        return PbftClient.class;
    }
}
