package byzzbench.simulator.protocols.XRPL;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.transport.Transport;

public class XRPLScenarioExecutor extends ScenarioExecutor<XRPLLedger>  {
    private final int NUM_NODES = 3;
    
    
    private List<XRPLReplica> replica_list;

    public XRPLScenarioExecutor() {
        super(new Transport<>());
        this.setNumClients(1);
    }

    @Override
    public void setup() {
        try {
            Set<String> nodeIds = new TreeSet<>();
            for (int i = 0; i < NUM_NODES; i++) {
                nodeIds.add(Character.toString((char) ('A' + i)));
            }
            this.replica_list = new ArrayList<>();
            XRPLLedger genesis = new XRPLLedger( "0", 1, new ArrayList<>());
            nodeIds.forEach(nodeId -> {
                //XRPLMessageLog messageLog = new XRPLMessageLog();
                XRPLReplica replica = new XRPLReplica(nodeId, nodeIds, this.transport, nodeIds, genesis); //nodes trust all nodes currently
                this.replica_list.add(replica);
                transport.addNode(replica);
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        this.runScenario1();        
    }
    
    /*
     * Scenario with 2 client requests of 2 different
     * transactions to 2 different nodes.
     */
    private void runScenario1() {
        try {
            this.transport.sendClientRequest("C0", "0000", "A");
            this.transport.sendClientRequest("C0", "0001", "B");

            this.initializeHeartbeats();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /*
     * Scenario of just 1 transaction
     */
    @SuppressWarnings("unused")
    private void runScenario2() {
        try {
            this.transport.sendClientRequest("c1", "0000", "A");

            this.initializeHeartbeats();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void initializeHeartbeats() {
        //The first heartbeat to initialize
        for (XRPLReplica xrplReplica : replica_list) {
            xrplReplica.onHeartbeat();
        }
    }

}
