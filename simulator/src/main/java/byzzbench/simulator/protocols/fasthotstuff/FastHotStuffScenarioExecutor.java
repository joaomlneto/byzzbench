package byzzbench.simulator.protocols.fasthotstuff;

import byzzbench.simulator.Replica;
import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.protocols.fasthotstuff.message.Block;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.service.SchedulesService;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component
@Log
public class FastHotStuffScenarioExecutor extends ScenarioExecutor<Block> {
    private final int NUM_NODES = 4;

    public FastHotStuffScenarioExecutor(MessageMutatorService messageMutatorService, SchedulesService schedulesService) {
        super("fasthotstuff", messageMutatorService, schedulesService);
    }

    @Override
    public void setup() {
        try {
            List<String> nodeIds = new ArrayList<>();
            for (int i = 0; i < NUM_NODES; i++) {
                nodeIds.add(Character.toString((char) ('A' + i)));
            }

            nodeIds.forEach(nodeId -> {
                Replica<Block> replica = new FastHotStuffReplica(nodeId, new HashSet<>(nodeIds), transport);
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
}
