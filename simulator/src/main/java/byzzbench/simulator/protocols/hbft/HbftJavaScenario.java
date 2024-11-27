package byzzbench.simulator.protocols.hbft;

import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.JsonNode;

import byzzbench.simulator.BaseScenario;
import byzzbench.simulator.Replica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;
import byzzbench.simulator.TerminationCondition;
import byzzbench.simulator.scheduler.Scheduler;
import lombok.extern.java.Log;

@Log
public class HbftJavaScenario extends BaseScenario {
    private final ScenarioPredicate terminationCondition = new ScenarioPredicate() {
        @Override
        public boolean test(Scenario context) {
            return false;
        }
    };

    public HbftJavaScenario(Scheduler scheduler) {
        super("hbft", scheduler);
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
                MessageLog messageLog = new MessageLog(100, 100, 200);
                Replica replica = new HbftJavaReplica<String, String>(nodeId, nodeIds, 1, 1000, messageLog, this);
                this.addNode(replica);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void run() {
        // send a request message to all nodes
        try {
            this.setNumClients(1);
            this.transport.multicastClientRequest("C0", System.currentTimeMillis(), "123", this.getNodes().keySet());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
