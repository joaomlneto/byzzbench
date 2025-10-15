package byzzbench.simulator.protocols.fasthotstuff;

import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import lombok.extern.java.Log;

import java.util.SortedSet;
import java.util.TreeSet;

@Log
public class FastHotStuffScenario extends Scenario {
    private static final String SCENARIO_ID = "fasthotstuff";
    private final int NUM_NODES = 4;

    public FastHotStuffScenario(Schedule schedule) {
        super(schedule, SCENARIO_ID);
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
                Replica replica = new FastHotStuffReplica(nodeId, this);
                this.addNode(replica);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void run() {
        // nothing to do at the moment
        // TODO: genesis block creation logic should be moved here
    }

    @Override
    public int maxFaultyReplicas(int n) {
        return (n - 1) / 3;
    }
}
