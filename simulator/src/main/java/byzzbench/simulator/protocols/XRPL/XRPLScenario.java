package byzzbench.simulator.protocols.XRPL;

import byzzbench.simulator.Client;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.scheduler.Scheduler;
import byzzbench.simulator.transport.DefaultClientRequestPayload;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class XRPLScenario extends Scenario {
    private final int NUM_NODES = 7;


    private List<XRPLReplica> replica_list;

    public XRPLScenario(Scheduler scheduler) {
        super("xrpl", scheduler);
    }

    @Override
    public void loadScenarioParameters(JsonNode parameters) {
        // no parameters to load
    }

    @Override
    protected void setup() {
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
                nodeIds.add(Character.toString((char) ('A' + i)));
                unl.add(Character.toString((char) ('A' + i)));
            }
            this.replica_list = new ArrayList<>();
            XRPLLedger genesis = new XRPLLedger("0", 1, new ArrayList<>());
            nodeIds.forEach(nodeId -> {
                //XRPLMessageLog messageLog = new XRPLMessageLog();
                XRPLReplica replica = new XRPLReplica(nodeId, this, unl, genesis); //nodes trust all nodes currently
                this.replica_list.add(replica);
                this.addNode(replica);
            });
            //transport.registerMessageMutators(new XRPLProposeMessageMutatorFactory());
            //transport.registerMessageMutators(new XRPLSubmitMessageMutatorFactory());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    private void setupForScenario3() {
        try {
            SortedSet<String> nodeIds = new TreeSet<>();
            for (int i = 0; i < 7; i++) {
                nodeIds.add(Character.toString((char) ('A' + i)));
            }
            this.replica_list = new ArrayList<>();
            XRPLLedger genesis = new XRPLLedger("0", 1, new ArrayList<>());

            List<String> unl1 = List.of("A", "B", "C", "D", "E");
            List<String> unl2 = List.of("C", "D", "E", "F", "G");

            XRPLReplica replica1 = new XRPLReplica("A", this, unl1, genesis);
            XRPLReplica replica2 = new XRPLReplica("B", this, unl1, genesis);
            XRPLReplica replica3 = new XRPLReplica("C", this, unl1, genesis);

            XRPLReplica replica4 = new XRPLReplica("D", this, List.of("D"), genesis);

            XRPLReplica replica5 = new XRPLReplica("E", this, unl2, genesis);
            XRPLReplica replica6 = new XRPLReplica("F", this, unl2, genesis);
            XRPLReplica replica7 = new XRPLReplica("G", this, unl2, genesis);

            this.replica_list.addAll(List.of(replica1, replica2, replica3, replica4, replica5, replica6, replica7));

            replica_list.forEach(r -> {
                this.addNode(r);
            });
            //transport.registerMessageMutators(new XRPLProposeMessageMutatorFactory());
            //transport.registerMessageMutators(new XRPLSubmitMessageMutatorFactory());
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
            this.sendClientRequest("C0", "0000", "A");
            this.sendClientRequest("C0", "0001", "B");

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
            this.sendClientRequest("c1", "0000", "A");

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
        System.out.println("Running scenario 3");
        try {
            this.addClient(new Client(this, "C0") {
                @Override
                public void initialize() {
                    this.getScenario().getTransport().sendMessage(
                            this,
                            new DefaultClientRequestPayload("tx"),
                            "D"
                    );
                    //this.getScenario().getTransport().sendClientRequest(this.getId(), "tx", "D");
                }
            });
            this.sendClientRequest("C0", "tx", "D");
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

    @Override
    public int maxFaultyReplicas(int n) {
        int maxFaultyReplicas = (this.getReplicas().size() - 1) / 5;
        if (maxFaultyReplicas == 0) {
            throw new IllegalArgumentException("XRP requires at least 6 replicas");
        }
        return maxFaultyReplicas;
    }

    /**
     * Sends a client request to a replica in the system.
     *
     * @param fromId    The ID of the client sending the request
     * @param operation The operation to be performed
     * @param toId      The ID of the replica receiving the request
     */
    private void sendClientRequest(String fromId, String operation, String toId) {
        Client from = this.getClients().get(fromId);
        DefaultClientRequestPayload payload = new DefaultClientRequestPayload(operation);
        this.getTransport().sendMessage(from, payload, toId);
    }
}
