package byzzbench.simulator.protocols.dummy;

import byzzbench.simulator.Replica;
import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.TerminationCondition;
import byzzbench.simulator.protocols.pbft_java.MessageLog;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.service.SchedulesService;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.TreeSet;

@Component
@Log
public class DummyScenarioExecutor extends ScenarioExecutor {

    public DummyScenarioExecutor(MessageMutatorService messageMutatorService, SchedulesService schedulesService) {
        super("dummy", messageMutatorService, schedulesService);
    }

    @Override
    public void setup() {
        try {
            Set<String> nodeIds = new TreeSet<>();
            for (int i = 0; i < 4; i++) {
                nodeIds.add(Character.toString((char) ('A' + i)));
            }

            nodeIds.forEach(nodeId -> {
                MessageLog messageLog = new MessageLog(100, 100, 200);
                Replica replica = new DummyReplica(nodeId, nodeIds, transport);
                this.addNode(replica);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void run() {
        try {
            //RequestMessage m = new RequestMessage("123", System.currentTimeMillis(), "c0");
            //nodes.get("A").handleMessage("c0", m);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public TerminationCondition getTerminationCondition() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTerminationCondition'");
    }
}
