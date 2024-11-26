package byzzbench.simulator.protocols.dummy;

import byzzbench.simulator.BaseScenario;
import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;
import byzzbench.simulator.scheduler.Scheduler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.SortedSet;
import java.util.TreeSet;

@Getter
@Log
public class DummyScenario extends BaseScenario {
    private final ScenarioPredicate terminationCondition = new ScenarioPredicate() {
        @Override
        public boolean test(Scenario scenario) {
            return false;
        }
    };

    public DummyScenario(Scheduler scheduler) {
        super("dummy", scheduler);
        setNumClients(2);
    }

    @Override
    public void loadScenarioParameters(JsonNode parameters) {
        // no parameters to load
    }

    @Override
    protected void setup() {
        try {
            SortedSet<String> nodeIds = new TreeSet<>();
            for (int i = 0; i < 2; i++) {
                nodeIds.add(Character.toString((char) ('A' + i)));
            }
            nodeIds.forEach(nodeId -> {
                Replica replica = new DummyReplica(nodeId, this);
                this.addNode(replica);
            });
            this.setNumClients(2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void run() {
        // nothing to do
    }
}
