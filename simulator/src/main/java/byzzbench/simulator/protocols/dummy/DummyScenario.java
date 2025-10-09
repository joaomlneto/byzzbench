package byzzbench.simulator.protocols.dummy;

import byzzbench.simulator.Client;
import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.protocols.pbft_java.PbftClient;
import lombok.Getter;
import lombok.extern.java.Log;

@Getter
@Log
public class DummyScenario extends Scenario {
    private static final String SCENARIO_ID = "dummy";
    private final ScenarioPredicate terminationCondition = new ScenarioPredicate() {
        @Override
        public boolean test(Scenario scenario) {
            return false;
        }
    };

    public DummyScenario(Schedule schedule) {
        super(schedule, SCENARIO_ID);
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
                Replica replica = new DummyReplica(Character.toString((char) ('A' + i)), this);
                this.addNode(replica);
            }

            // add clients
            for (int i = 0; i < 2; i++) {
                String clientId = String.format("C%d", i);
                Client client = new PbftClient(this, clientId);
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
        // this is a dummy protocol, so anything goes
        return n;
    }
}
