package byzzbench.simulator.protocols.XRPL;

import java.util.Set;
import java.util.TreeSet;

import byzzbench.simulator.Replica;
import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.transport.Transport;
import lombok.extern.java.Log;

@Log
public class XRPLScenarioExecutor extends ScenarioExecutor<XRPLLedger>  {
    private final int NUM_NODES = 5;

    public XRPLScenarioExecutor() {
        super(new Transport<>());
    }

    @Override
    public void setup() {
        try {
            Set<String> nodeIds = new TreeSet<>();
            for (int i = 0; i < NUM_NODES; i++) {
                nodeIds.add(Character.toString((char) ('A' + i)));
            }

            nodeIds.forEach(nodeId -> {
                //XRPLMessageLog messageLog = new XRPLMessageLog();
                Replica<XRPLLedger> replica = new XRPLReplica(nodeId, nodeIds, this.transport, nodeIds); //nodes trust all nodes currently
                nodes.put(nodeId, replica);
                transport.addNode(replica);
            });

            log.info("nodes: " + nodes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'run'");
    }

}
