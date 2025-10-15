package byzzbench.simulator.protocols.pbft_java;

import byzzbench.simulator.Client;
import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import lombok.extern.java.Log;

import java.time.Duration;

/**
 * A scenario for running the <a href="https://github.com/caojohnny/pbft-java">PBFT-Java protocol</a>, an implementation
 * of the PBFT protocol in Java.
 */
@Log
public class PbftJavaScenario extends Scenario {
    private final int numReplicas = 4;

    /**
     * Creates a new scenario with the given unique identifier and exploration_strategy.
     *
     * @param schedule The schedule for the scenario.
     */
    public PbftJavaScenario(Schedule schedule) {
        super(schedule);
    }

    @Override
    public void loadScenarioParameters(ScenarioParameters parameters) {
        // no parameters to load
    }

    @Override
    protected void setup() {
        // add replicas
        for (int i = 0; i < numReplicas; i++) {
            MessageLog messageLog = new MessageLog(100, 100, 200);
            Replica replica = new PbftJavaReplica<String, String>(Character.toString((char) ('A' + i)), this, 1, Duration.ofSeconds(1), messageLog);
            this.addNode(replica);
        }

        // add clients
        for (int i = 0; i < 1; i++) {
            String clientId = String.format("C%d", i);
            Client client = new PbftClient(this, clientId);
            this.addClient(client);
        }
    }

    @Override
    public synchronized void run() {
        // nothing to do
    }

    @Override
    public Replica cloneReplica(Replica replica) {
        MessageLog messageLog = new MessageLog(100, 100, 200);
        return new PbftJavaReplica<String, String>(replica.getId(), this, 1, Duration.ofSeconds(1), messageLog);
    }

    @Override
    public int maxFaultyReplicas(int n) {
        return (n - 1) / 3;
    }

    @Override
    public Class<? extends Replica> getReplicaClass() {
        return PbftJavaReplica.class;
    }

    @Override
    public Class<? extends Client> getClientClass() {
        return PbftClient.class;
    }
}
