package byzzbench.simulator.protocols.fab;

import byzzbench.simulator.BaseScenario;
import byzzbench.simulator.Replica;
import byzzbench.simulator.scheduler.Scheduler;
import byzzbench.simulator.scheduler.twins.TwinsScheduler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.java.Log;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Log
public class FastByzantineScenario extends BaseScenario {
//    public FastByzantineScenario(Scheduler scheduler) {
//        super("fab-java", scheduler);
//        this.terminationCondition = new FastByzantineTerminationCondition();
//    }

    public FastByzantineScenario(Scheduler scheduler) {
        super("fab-java", scheduler);
        this.terminationCondition = new FastByzantineTerminationCondition();
    }

    /**
     * Non-parameterized constructor.
     */

    @Override
    protected void loadScenarioParameters(JsonNode parameters) {
        // no parameters to load
    }

    @Override
    protected void setup() {
        // Scenario with f = 1 (Byzantine nodes), p = 4, a = 6, l = 4.
        int p = 4;
        int a = 6;
        int l = 4;
        int f = 1;
        long timeout = 15000L;
        try {
            SortedSet<String> nodesIds = new TreeSet<>();
            int NUM_NODES = 6;
            for (int i = 0; i < NUM_NODES; i++) {
                nodesIds.add(Character.toString((char) ('A' + i)));
            }

            List<Role> proposerAndAcceptor = List.of(Role.PROPOSER, Role.ACCEPTOR);
            FastByzantineReplica replica1 = new FastByzantineReplica(
                    "A",
                    nodesIds, transport, this, proposerAndAcceptor, true, timeout, p, a, l, f,
                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F")),
                    new TreeSet<>(List.of("C", "D", "E", "F")),
                    new TreeSet<>(List.of("A", "B", "C", "D")),
                    "A",
                    "C0");
            this.addNode(replica1);

            FastByzantineReplica replica2 = new FastByzantineReplica("B", nodesIds, transport, this, proposerAndAcceptor, false, timeout, p, a, l, f,
                    new TreeSet<>(List.of("A", "B" ,"C", "D", "E", "F")),
                    new TreeSet<>(List.of("C", "D", "E", "F")),
                    new TreeSet<>(List.of("A", "B", "C", "D")),
                    "A",
                    "C0");
            this.addNode(replica2);

            List<Role> proposerAndAcceptorAndLearner = List.of(Role.PROPOSER, Role.ACCEPTOR, Role.LEARNER);

            FastByzantineReplica replica3 = new FastByzantineReplica("C", nodesIds, transport, this, proposerAndAcceptorAndLearner, false,2 * timeout, p, a, l, f,
                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F")),
                    new TreeSet<>(List.of("C", "D", "E", "F")),
                    new TreeSet<>(List.of("A", "B", "C", "D")),
                    "A",
                    "C0");
            this.addNode(replica3);

            FastByzantineReplica replica4 = new FastByzantineReplica("D", nodesIds, transport, this, proposerAndAcceptorAndLearner, false,3 * timeout, p, a, l, f,
                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F")),
                    new TreeSet<>(List.of("C", "D", "E", "F")),
                    new TreeSet<>(List.of("A", "B", "C", "D")),
                    "A",
                    "C0");
            this.addNode(replica4);

            List<Role> learnerAndAcceptor = List.of(Role.ACCEPTOR, Role.LEARNER);
            FastByzantineReplica replica5 = new FastByzantineReplica("E", nodesIds, transport, this, learnerAndAcceptor, false,4 * timeout, p, a, l, f,
                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F")),
                    new TreeSet<>(List.of("C", "D", "E", "F")),
                    new TreeSet<>(List.of("A", "B", "C", "D")),
                    "A",
                    "C0");
            this.addNode(replica5);

            FastByzantineReplica replica6 = new FastByzantineReplica("F", nodesIds, transport, this, learnerAndAcceptor, false,5 * timeout, p, a, l, f,
                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F")),
                    new TreeSet<>(List.of("C", "D", "E", "F")),
                    new TreeSet<>(List.of("A", "B", "C", "D")),
                    "A",
                    "C0");
            this.addNode(replica6);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    @Override
//    protected void setup() {
//        // Scenario with f = 1 (Byzantine nodes), p = 4, a = 6, l = 4.
//        int p = 7;
//        int a = 11;
//        int l = 9;
//        int f = 2;
//        long timeout = 15000L;
//        try {
//            SortedSet<String> nodesIds = new TreeSet<>();
//            int NUM_NODES = 11;
//            for (int i = 0; i < NUM_NODES; i++) {
//                nodesIds.add(Character.toString((char) ('A' + i)));
//            }
//
//            List<Role> proposerAndAcceptor = List.of(Role.PROPOSER, Role.ACCEPTOR);
//            List<Role> proposerAndAcceptorAndLearner = List.of(Role.PROPOSER, Role.ACCEPTOR, Role.LEARNER);
//            List<Role> learnerAndAcceptor = List.of(Role.ACCEPTOR, Role.LEARNER);
//
//            // 1 p 1 a 0 l
//            FastByzantineReplica replica1 = new FastByzantineReplica(
//                    "A",
//                    nodesIds, transport, this, proposerAndAcceptor, true, timeout, p, a, l, f,
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G")),
//                    "A",
//                    "C0");
//            this.addNode(replica1);
//
//            // 2 p 2 a 0 l
//            FastByzantineReplica replica2 = new FastByzantineReplica("B", nodesIds, transport, this, proposerAndAcceptor, false, timeout, p, a, l, f,
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G")),
//                    "A",
//                    "C0");
//            this.addNode(replica2);
//
//            // 3 p 3 a 1 l
//            FastByzantineReplica replica3 = new FastByzantineReplica("C", nodesIds, transport, this, proposerAndAcceptorAndLearner, false,2 * timeout, p, a, l, f,
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G")),
//                    "A",
//                    "C0");
//            this.addNode(replica3);
//
//            // 4 p 4 a 2 l
//            FastByzantineReplica replica4 = new FastByzantineReplica("D", nodesIds, transport, this, proposerAndAcceptorAndLearner, false,3 * timeout, p, a, l, f,
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G")),
//                    "A",
//                    "C0");
//            this.addNode(replica4);
//
//            // 5 p 5 a 3 l
//            FastByzantineReplica replica5 = new FastByzantineReplica("E", nodesIds, transport, this, proposerAndAcceptorAndLearner, false,4 * timeout, p, a, l, f,
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G")),
//                    "A",
//                    "C0");
//            this.addNode(replica5);
//
//            // 6 p 6 a 4  l
//            FastByzantineReplica replica6 = new FastByzantineReplica("F", nodesIds, transport, this, proposerAndAcceptorAndLearner, false,5 * timeout, p, a, l, f,
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G")),
//                    "A",
//                    "C0");
//            this.addNode(replica6);
//
//            // 7 p 7 a 5 l
//            FastByzantineReplica replica7 = new FastByzantineReplica("G", nodesIds, transport, this, proposerAndAcceptorAndLearner, false,5 * timeout, p, a, l, f,
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G")),
//                    "A",
//                    "C0");
//            this.addNode(replica7);
//
//            // 7 p 8 a 6 l
//            FastByzantineReplica replica8 = new FastByzantineReplica("H", nodesIds, transport, this, learnerAndAcceptor, false,5 * timeout, p, a, l, f,
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G")),
//                    "A",
//                    "C0");
//            this.addNode(replica8);
//
//            // 7 p 9 a 7 l
//            FastByzantineReplica replica9 = new FastByzantineReplica("I", nodesIds, transport, this, learnerAndAcceptor, false,5 * timeout, p, a, l, f,
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G")),
//                    "A",
//                    "C0");
//            this.addNode(replica9);
//
//            // 7 p 10 a 8 l
//            FastByzantineReplica replica10 = new FastByzantineReplica("J", nodesIds, transport, this, learnerAndAcceptor, false,5 * timeout, p, a, l, f,
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G")),
//                    "A",
//                    "C0");
//            this.addNode(replica10);
//
//            // 7 p 11 a 9 l
//            FastByzantineReplica replica11 = new FastByzantineReplica("K", nodesIds, transport, this, learnerAndAcceptor, false,5 * timeout, p, a, l, f,
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("C", "D", "E", "F", "G", "H", "I", "J", "K")),
//                    new TreeSet<>(List.of("A", "B", "C", "D", "E", "F", "G")),
//                    "A",
//                    "C0");
//            this.addNode(replica11);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

//    @Override
//    protected void run() {
//        try {
////            FastByzantineClient client = new FastByzantineClient(this, "C0", List.of("C", "D", "E", "F"), List.of("A", "B", "C", "D"));
//            FastByzantineClient client = new FastByzantineClient(
//                    this,
//                    "C0",
//                    List.of("C", "D", "E", "F", "G", "H", "I", "J", "K"),
//                    List.of("A", "B", "C", "D", "E", "F", "G"));
//            this.addClient(client);
//            this.transport.sendClientRequest("C0", "123", "A");
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    @Override
    protected void run() {
        try {
            FastByzantineClient client = new FastByzantineClient(this, "C0", List.of("C", "D", "E", "F"), List.of("A", "B", "C", "D"));
//            FastByzantineClient client = new FastByzantineClient(
//                    this,
//                    "C0",
//                    List.of("C", "D", "E", "F", "G", "H", "I", "J", "K"),
//                    List.of("A", "B", "C", "D", "E", "F", "G"));
            this.addClient(client);
            this.transport.sendClientRequest("C0", "123", "A");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parametrized constructor.
     */

//    @Override
//    protected void loadScenarioParameters(JsonNode parameters) {
//        // no parameters to load
//    }

//    @Override
//    protected void setup() {
//        // Scenario with f = 1 (Byzantine nodes), p = 4, a = 6, l = 4.
//        int p = 4;
//        int a = 4;
//        int l = 4;
//        int f = 1;
//        long timeout = 9000L;
//        try {
//            SortedSet<String> nodesIds = new TreeSet<>();
//            int NUM_NODES = 4;
//            for (int i = 0; i < NUM_NODES; i++) {
//                nodesIds.add(Character.toString((char) ('A' + i)));
//            }
//
//            List<Role> proposerAndAcceptorAndLearner = List.of(Role.PROPOSER, Role.ACCEPTOR, Role.LEARNER);
//            // This is the Byzantine node in this scenario.
//            FastByzantineReplica replica1 = new FastByzantineReplica(
//                    "A",
//                    nodesIds, transport, this, proposerAndAcceptorAndLearner, true, timeout, p, a, l, f,
//                    new TreeSet<>(List.of("A", "B", "C", "D")),
//                    new TreeSet<>(List.of("A", "B", "C", "D")),
//                    new TreeSet<>(List.of("A", "B", "C", "D")),
//                    "A",
//                    "C0");
//            this.addNode(replica1);
//
//            FastByzantineReplica replica2 = new FastByzantineReplica("B", nodesIds, transport, this, proposerAndAcceptorAndLearner, false, 2 * timeout, p, a, l, f,
//                    new TreeSet<>(List.of("A", "B", "C", "D")),
//                    new TreeSet<>(List.of("A", "B", "C", "D")),
//                    new TreeSet<>(List.of("A", "B", "C", "D")),
//                    "A",
//                    "C0");
//            this.addNode(replica2);
//
//            FastByzantineReplica replica3 = new FastByzantineReplica("C", nodesIds, transport, this, proposerAndAcceptorAndLearner, false, 2 * timeout, p, a, l, f,
//                    new TreeSet<>(List.of("A", "B", "C", "D")),
//                    new TreeSet<>(List.of("A", "B", "C", "D")),
//                    new TreeSet<>(List.of("A", "B", "C", "D")),
//                    "A",
//                    "C0");
//            this.addNode(replica3);
//
//            FastByzantineReplica replica4 = new FastByzantineReplica("D", nodesIds, transport, this, proposerAndAcceptorAndLearner, false, 2* timeout, p, a, l, f,
//                    new TreeSet<>(List.of("A", "B", "C", "D")),
//                    new TreeSet<>(List.of("A", "B", "C", "D")),
//                    new TreeSet<>(List.of("A", "B", "C", "D")),
//                    "A",
//                    "C0");
//            this.addNode(replica4);
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    protected void run() {
//        // send a request message to node A
//        try {
//            this.setNumClients(1);
//            this.transport.sendClientRequest("C0", "123", "A");
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    @Override
    public Replica cloneReplica(Replica replica) {
        return super.cloneReplica(replica);
    }

    @Override
    public int maxFaultyReplicas(int n) {
        return 1;
    }

}