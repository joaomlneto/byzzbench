package byzzbench.simulator.protocols.fasthotstuff;

import byzzbench.simulator.Replica;
import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.protocols.fasthotstuff.faults.FHSBugFaults;
import byzzbench.simulator.protocols.fasthotstuff.message.Block;
import byzzbench.simulator.transport.Transport;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.TreeSet;

@Component
@Log
public class FastHotStuffScenarioExecutor extends ScenarioExecutor<Block> {
    private final int NUM_NODES = 4;

    public FastHotStuffScenarioExecutor() {
        super("fasthotstuff", new Transport());
    }

    @Override
    public void setup() {
        try {
            Set<String> nodeIds = new TreeSet<>();
            for (int i = 0; i < NUM_NODES; i++) {
                nodeIds.add(Character.toString((char) ('A' + i)));
            }

            nodeIds.forEach(nodeId -> {
                Replica<Block> replica = new FastHotStuffReplica(nodeId, nodeIds, transport);
                this.addNode(replica);
            });

            transport.addFaults(new FHSBugFaults().getFaults());
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
