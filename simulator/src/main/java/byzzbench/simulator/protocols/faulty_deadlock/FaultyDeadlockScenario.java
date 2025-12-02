package byzzbench.simulator.protocols.faulty_deadlock;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.nodes.Client;
import byzzbench.simulator.nodes.Replica;
import lombok.Getter;
import lombok.extern.java.Log;

@Getter
@Log
public class FaultyDeadlockScenario extends Scenario {

    /**
     * Creates a new scenario from the given schedule.
     *
     * @param schedule The schedule for the scenario.
     */
    public FaultyDeadlockScenario(Schedule schedule) {
        super(schedule);
    }

    @Override
    public void loadScenarioParameters(ScenarioParameters parameters) {
        // no parameters to load
    }

    @Override
    protected void setup() {
        try {
            // add replicas
            for (int i = 0; i < 2; i++) {
                Replica replica = new FaultyDeadlockReplica(Character.toString((char) ('A' + i)), this);
                this.addNode(replica);
            }

            // add clients
            for (int i = 0; i < 1; i++) {
                String clientId = String.format("C%d", i);
                Client client = new FaultyDeadlockClient(this, clientId);
                this.addClient(client);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void run() {
        // nothing to do
    }

    @Override
    public int maxFaultyReplicas(int n) {
        // this is a faulty_safety protocol, so anything goes
        return n;
    }

    @Override
    public Class<? extends Replica> getReplicaClass() {
        return FaultyDeadlockReplica.class;
    }

    @Override
    public Class<? extends Client> getClientClass() {
        return FaultyDeadlockClient.class;
    }
}
