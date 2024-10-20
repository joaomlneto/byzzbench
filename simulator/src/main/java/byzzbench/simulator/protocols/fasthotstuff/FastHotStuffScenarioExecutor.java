package byzzbench.simulator.protocols.fasthotstuff;

import byzzbench.simulator.BaseScenario;
import byzzbench.simulator.Replica;
import byzzbench.simulator.TerminationCondition;
import byzzbench.simulator.scheduler.Scheduler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.java.Log;

import java.util.SortedSet;
import java.util.TreeSet;

@Log
public class FastHotStuffScenarioExecutor extends BaseScenario {
    private final int NUM_NODES = 4;

    public FastHotStuffScenarioExecutor(Scheduler scheduler) {
        super("fasthotstuff", scheduler);
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
                Replica replica = new FastHotStuffReplica(nodeId, nodeIds, transport, timekeeper);
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
    public TerminationCondition getTerminationCondition() {
        return new TerminationCondition() {
            @Override
            public boolean shouldTerminate() {
                return false;
            }
        };
    }
}
