package byzzbench.simulator.protocols.XRPL;

import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.TerminationCondition;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.service.SchedulesService;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.springframework.stereotype.Component;

@Component
public class XRPLScenarioExecutor extends ScenarioExecutor {
  private final int NUM_NODES = 7;

  private List<XRPLReplica> replica_list;
  private XRPLTerminationCondition terminationCondition;

  public XRPLScenarioExecutor(MessageMutatorService messageMutatorService,
                              SchedulesService schedulesService) {
    super("xrpl", messageMutatorService, schedulesService);
    this.setNumClients(1);
  }

  @Override
  public void loadScenarioParameters(JsonNode parameters) {
    // no parameters to load
  }

  @Override
  public void setup() {
    setupForScenario3();
    this.terminationCondition = new XRPLTerminationCondition(replica_list);
  }

  @Override
  public void run() {
    this.runScenario3();
  }

  /*
   * The setup for scenarios 1 and 2
   */
  @SuppressWarnings("unused")
  private void setupDefault() {
    try {
      SortedSet<String> nodeIds = new TreeSet<>();
      List<String> unl = new ArrayList<>();
      for (int i = 0; i < NUM_NODES; i++) {
        nodeIds.add(Character.toString((char)('A' + i)));
        unl.add(Character.toString((char)('A' + i)));
      }
      this.replica_list = new ArrayList<>();
      XRPLLedger genesis = new XRPLLedger("0", 1, new ArrayList<>());
      nodeIds.forEach(nodeId -> {
        // XRPLMessageLog messageLog = new XRPLMessageLog();
        XRPLReplica replica =
            new XRPLReplica(nodeId, nodeIds, this.transport, unl,
                            genesis); // nodes trust all nodes currently
        this.replica_list.add(replica);
        transport.addNode(replica);
      });
      // transport.registerMessageMutators(new
      // XRPLProposeMessageMutatorFactory());
      // transport.registerMessageMutators(new
      // XRPLSubmitMessageMutatorFactory());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  @SuppressWarnings("unused")
  private void setupForScenario3() {
    try {
      SortedSet<String> nodeIds = new TreeSet<>();
      for (int i = 0; i < 7; i++) {
        nodeIds.add(Character.toString((char)('A' + i)));
      }
      this.replica_list = new ArrayList<>();
      XRPLLedger genesis = new XRPLLedger("0", 1, new ArrayList<>());

      List<String> unl1 = List.of("A", "B", "C", "D", "E");
      List<String> unl2 = List.of("C", "D", "E", "F", "G");

      XRPLReplica replica1 =
          new XRPLReplica("A", nodeIds, this.transport, unl1, genesis);
      XRPLReplica replica2 =
          new XRPLReplica("B", nodeIds, this.transport, unl1, genesis);
      XRPLReplica replica3 =
          new XRPLReplica("C", nodeIds, this.transport, unl1, genesis);

      XRPLReplica replica4 =
          new XRPLReplica("D", nodeIds, this.transport, List.of("D"), genesis);

      XRPLReplica replica5 =
          new XRPLReplica("E", nodeIds, this.transport, unl2, genesis);
      XRPLReplica replica6 =
          new XRPLReplica("F", nodeIds, this.transport, unl2, genesis);
      XRPLReplica replica7 =
          new XRPLReplica("G", nodeIds, this.transport, unl2, genesis);

      this.replica_list.addAll(List.of(replica1, replica2, replica3, replica4,
                                       replica5, replica6, replica7));

      replica_list.forEach(r -> { transport.addNode(r); });
      // transport.registerMessageMutators(new
      // XRPLProposeMessageMutatorFactory());
      // transport.registerMessageMutators(new
      // XRPLSubmitMessageMutatorFactory());
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

  @Override
  public TerminationCondition getTerminationCondition() {
    return this.terminationCondition;
  }
}
