package byzzbench.simulator.protocols.event_driven_hotstuff;

import byzzbench.simulator.BaseScenario;
import byzzbench.simulator.Replica;
import byzzbench.simulator.TerminationCondition;
import byzzbench.simulator.scheduler.Scheduler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.java.Log;

import java.util.SortedSet;
import java.util.TreeSet;

@Log
public class EDHotStuffScenario extends BaseScenario {
    private final int NUM_NODES = 4;

    public EDHotStuffScenario(Scheduler scheduler) {
        super("ed-hotstuff", scheduler);
    }

    @Override
    protected void loadScenarioParameters(JsonNode parameters) {

    }

    @Override
    protected void setup() {
        try {
            SortedSet<String> nodeIds = new TreeSet<>();
            for (int i = 0; i < NUM_NODES; i++) nodeIds.add(Character.toString((char) ('A' + i)));

            nodeIds.forEach(nodeId -> {
                Replica replica = new EDHotStuffReplica(nodeId, nodeIds, transport);
                System.out.println(nodeId);
                this.addNode(replica);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void run() {
        try {
            this.setNumClients(1);
            this.transport.sendClientRequest("C0", "123", "A");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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
