package byzzbench.simulator.protocols.hbft;

import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.JsonNode;

import byzzbench.simulator.BaseScenario;
import byzzbench.simulator.Replica;
import byzzbench.simulator.scheduler.Scheduler;
import lombok.Getter;
import lombok.extern.java.Log;

@Getter
@Log
public class HbftJavaScenario extends BaseScenario {
    private final int NUM_NODES = 4;
    private final HbftTerminationCondition terminationCondition;
    private SortedSet<String> nodeIds;

    public HbftJavaScenario(Scheduler scheduler) {
        super("hbft", scheduler);
        this.terminationCondition = new HbftTerminationCondition();
    }

    @Override
    public void loadScenarioParameters(JsonNode parameters) {
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
}
