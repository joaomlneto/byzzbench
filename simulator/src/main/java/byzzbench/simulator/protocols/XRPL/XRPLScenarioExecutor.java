package byzzbench.simulator.protocols.XRPL;

import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.protocols.XRPL.messages.XRPLTxMessage;
import byzzbench.simulator.transport.Transport;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import lombok.extern.java.Log;

@Log
public class XRPLScenarioExecutor extends ScenarioExecutor<XRPLLedger> {
  private final int NUM_NODES = 3;

  private List<XRPLReplica> replica_list;

  public XRPLScenarioExecutor() { super(new Transport<>()); }

  @Override
  public void setup() {
    try {
      Set<String> nodeIds = new TreeSet<>();
      for (int i = 0; i < NUM_NODES; i++) {
        nodeIds.add(Character.toString((char)('A' + i)));
      }
      this.replica_list = new ArrayList<>();
      XRPLLedger genesis = new XRPLLedger("0", 1, new ArrayList<>());
      nodeIds.forEach(nodeId -> {
        // XRPLMessageLog messageLog = new XRPLMessageLog();
        XRPLReplica replica =
            new XRPLReplica(nodeId, nodeIds, this.transport, nodeIds,
                            genesis); // nodes trust all nodes currently
        this.replica_list.add(replica);
        nodes.put(nodeId, replica);
        transport.addNode(replica);
      });
      transport.registerMessageMutators(new XRPLProposeMessageMutatorFactory());
      log.info("nodes: " + nodes);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void run() {
    try {
      String tx1 = "0000";
      String tx2 = "0001";

      XRPLTxMessage txmsg1 = new XRPLTxMessage(tx1);
      XRPLTxMessage txmsg2 = new XRPLTxMessage(tx2);

      nodes.get("A").handleMessage("c", txmsg1);
      nodes.get("B").handleMessage("c1", txmsg2);

      // The first heartbeat to initialize
      for (XRPLReplica xrplReplica : replica_list) {
        xrplReplica.onHeartbeat();
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
