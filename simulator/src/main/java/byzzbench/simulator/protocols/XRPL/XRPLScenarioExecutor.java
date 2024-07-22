package byzzbench.simulator.protocols.XRPL;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.protocols.XRPL.messages.XRPLTxMessage;
import byzzbench.simulator.transport.Transport;
import lombok.extern.java.Log;

@Log
public class XRPLScenarioExecutor extends ScenarioExecutor<XRPLLedger>  {
    private final int NUM_NODES = 3;
    
    
    private List<XRPLReplica> replica_list;

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
            this.replica_list = new ArrayList<>();
            XRPLLedger genesis = new XRPLLedger("1", "0", 1);
            nodeIds.forEach(nodeId -> {
                //XRPLMessageLog messageLog = new XRPLMessageLog();
                XRPLReplica replica = new XRPLReplica(nodeId, nodeIds, this.transport, nodeIds, genesis); //nodes trust all nodes currently
                this.replica_list.add(replica);
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
        try {
            String tx1 = "0000";

            XRPLTxMessage txmsg1 = new XRPLTxMessage(tx1);

            nodes.get("A").handleMessage("c", txmsg1);
            
            for (XRPLReplica xrplReplica : replica_list) {
                Runnable r = new XRPLHeartbeatRunnable(xrplReplica);
                this.transport.setTimeout(xrplReplica, r, 5000);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        
    }

}
