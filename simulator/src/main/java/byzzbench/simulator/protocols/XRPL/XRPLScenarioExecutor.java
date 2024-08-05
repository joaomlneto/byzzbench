package byzzbench.simulator.protocols.XRPL;

import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.protocols.XRPL.mutators.XRPLProposeMessageMutatorFactory;
import byzzbench.simulator.protocols.XRPL.mutators.XRPLSubmitMessageMutatorFactory;
import byzzbench.simulator.transport.Transport;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class XRPLScenarioExecutor extends ScenarioExecutor<XRPLLedger> {
  private final int NUM_NODES = 3;

  private List<XRPLReplica> replica_list;

  public XRPLScenarioExecutor() {
    super(new Transport<>());
    this.setNumClients(1);
  }

  @Override
  public void setup() {
    setupDefault();
  }

  @Override
  public void run() {
    this.runScenario1();
  }

  /*
   * The setup for scenarios 1 and 2
   */
  @SuppressWarnings("unused")
  private void setupDefault() {
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
        transport.addNode(replica);
      });
      transport.registerMessageMutators(new XRPLProposeMessageMutatorFactory());
      transport.registerMessageMutators(new XRPLSubmitMessageMutatorFactory());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  @SuppressWarnings("unused")
  private void setupForScenario3() {
    try {
      Set<String> nodeIds = new TreeSet<>();
      for (int i = 0; i < 7; i++) {
        nodeIds.add(Character.toString((char)('A' + i)));
      }
      this.replica_list = new ArrayList<>();
      XRPLLedger genesis = new XRPLLedger("0", 1, new ArrayList<>());

      Set<String> unl1 = Set.of("A", "B", "C", "D", "E");
      Set<String> unl2 = Set.of("C", "D", "E", "F", "G");

      XRPLReplica replica1 =
          new XRPLReplica("A", nodeIds, this.transport, unl1, genesis);
      XRPLReplica replica2 =
          new XRPLReplica("B", nodeIds, this.transport, unl1, genesis);
      XRPLReplica replica3 =
          new XRPLReplica("C", nodeIds, this.transport, unl1, genesis);

      XRPLReplica replica4 =
          new XRPLReplica("D", nodeIds, this.transport, nodeIds, genesis);

      XRPLReplica replica5 =
          new XRPLReplica("E", nodeIds, this.transport, unl2, genesis);
      XRPLReplica replica6 =
          new XRPLReplica("F", nodeIds, this.transport, unl2, genesis);
      XRPLReplica replica7 =
          new XRPLReplica("G", nodeIds, this.transport, unl2, genesis);

      this.replica_list.addAll(Set.of(replica1, replica2, replica3, replica4,
                                      replica5, replica6, replica7));

      replica_list.forEach(r -> { transport.addNode(r); });
      transport.registerMessageMutators(new XRPLProposeMessageMutatorFactory());
      transport.registerMessageMutators(new XRPLSubmitMessageMutatorFactory());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /*
   * Scenario with 2 client requests of 2 different
   * transactions to 2 different nodes.
   */
  @SuppressWarnings("unused")
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

  /*
   * The scenario of agreement violation presented in
   * the analysis paper.
   */
  @SuppressWarnings("unused")
  private void runScenario3() {
    try {
      this.transport.sendClientRequest("C0", "tx", "D");
      this.initializeHeartbeats();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private void initializeHeartbeats() {
    // The first heartbeat to initialize
    for (XRPLReplica xrplReplica : replica_list) {
      xrplReplica.onHeartbeat();
    }
  }
}
