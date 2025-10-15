package byzzbench.simulator.protocols.hbft;

import byzzbench.simulator.Client;
import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.SortedSet;
import java.util.TreeSet;

@Getter
@Log
public class HbftJavaScenario extends Scenario {
    private static final String SCENARIO_ID = "hbft";
    private final int NUM_NODES = 4;
    private SortedSet<String> nodeIds;

    public HbftJavaScenario(Schedule schedule) {
        super(schedule, SCENARIO_ID);
    }

    @Override
    public void loadScenarioParameters(ScenarioParameters parameters) {
        // no parameters to load
    }

    @Override
    protected void setup() {
        try {
            this.nodeIds = new TreeSet<>();
            for (int i = 0; i < 4; i++) {
                this.nodeIds.add(Character.toString((char) ('A' + i)));
            }

            this.nodeIds.forEach(nodeId -> {
                MessageLog messageLog = new MessageLog(100, 100, 200);
                Replica replica = new HbftJavaReplica<String, String>(nodeId, nodeIds, 1, 2, messageLog, this);
                this.addNode(replica);
            });
            this.setNumHbftClients(2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the number of hBFT clients in the scenario.
     *
     * @param numClients The number of clients to set.
     */
    protected void setNumHbftClients(int numClients) {
        for (int i = 0; i < numClients; i++) {
            String clientId = String.format("C%d", i);
            Client client = new HbftClient(this, clientId);
            this.addClient(client);
        }
    }

    @Override
    public synchronized void run() {
        // send a request message to all nodes
        // try {
        //     this.setNumClients(1);
        //     this.transport.multicastClientRequest("C0", System.currentTimeMillis(), "123", this.getNodes().keySet());
        // } catch (Exception e) {
        //     e.printStackTrace();
        //     throw new RuntimeException(e);
        // }
    }

    @Override
    public Replica cloneReplica(Replica replica) {
        MessageLog messageLog = new MessageLog(100, 100, 200);
        return new HbftJavaReplica<String, String>(replica.getId(), this.nodeIds, 1, 2, messageLog, this);
    }

    @Override
    public int maxFaultyReplicas(int n) {
        return (n - 1) / 3;
    }

    @Override
    public Class<? extends Replica> getReplicaClass() {
        return HbftJavaReplica.class;
    }

    @Override
    public Class<? extends Client> getClientClass() {
        return HbftClient.class;
    }
}
