package byzzbench.simulator.protocols.fab;

import byzzbench.simulator.BaseScenario;
import byzzbench.simulator.TerminationCondition;
import byzzbench.simulator.protocols.fab.replicas.FabReplica;
import byzzbench.simulator.protocols.fab.replicas.FabRole;
import byzzbench.simulator.scheduler.Scheduler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Log
public class FabScenario extends BaseScenario {
    private final int NUM_NODES = 6;
    private List<FabReplica> replicas;

    public FabScenario(Scheduler scheduler) {
        super("fab-java", scheduler);
    }
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
        long timeout = 50000L;
        try {
            SortedSet<String> nodesIds = new TreeSet<>();
            for (int i = 0; i < NUM_NODES; i++) {
                nodesIds.add(Character.toString((char) ('A' + i)));
            }
            replicas = new ArrayList<>();
            SortedSet<String> acceptors = new TreeSet<>(List.of("A", "B", "C", "D", "E", "F"));
            SortedSet<String> learners = new TreeSet<>(List.of("C", "D", "E", "F"));
            SortedSet<String> proposers = new TreeSet<>(List.of("A", "B", "C", "D"));
            List<FabRole> proposerAndAcceptor = List.of(FabRole.PROPOSER, FabRole.ACCEPTOR);
            FabReplica replica1 = new FabReplica(
                    "A",
                    nodesIds, transport, null, proposerAndAcceptor, true, timeout, p, a, l, f,
                    new TreeSet<>(List.of("B", "C", "D", "E", "F")),
                    new TreeSet<>(List.of("C", "D", "E", "F")),
                    new TreeSet<>(List.of("B", "C", "D")),
                    "A");
            replicas.add(replica1);
            this.addNode(replica1);

            FabReplica replica2 = new FabReplica("B", nodesIds, transport, null, proposerAndAcceptor, false, timeout, p, a, l, f,
                    new TreeSet<>(List.of("A", "C", "D", "E", "F")),
                    new TreeSet<>(List.of("C", "D", "E", "F")),
                    new TreeSet<>(List.of("A", "C", "D")),
                    "A");
            replicas.add(replica2);
            this.addNode(replica2);

            List<FabRole> proposerAndAcceptorAndLearner = List.of(FabRole.PROPOSER, FabRole.ACCEPTOR, FabRole.LEARNER);

            FabReplica replica3 = new FabReplica("C", nodesIds, transport, null, proposerAndAcceptorAndLearner, false, timeout, p, a, l, f,
                    new TreeSet<>(List.of("A", "B", "D", "E", "F")),
                    new TreeSet<>(List.of("D", "E", "F")),
                    new TreeSet<>(List.of("A", "B", "D")),
                    "A");
            replicas.add(replica3);
            this.addNode(replica3);

            FabReplica replica4 = new FabReplica("D", nodesIds, transport, null, proposerAndAcceptorAndLearner, false, timeout, p, a, l, f,
                    new TreeSet<>(List.of("A", "B", "C", "E", "F")),
                    new TreeSet<>(List.of("C", "E", "F")),
                    new TreeSet<>(List.of("A", "B", "C")),
                    "A");
            replicas.add(replica4);
            this.addNode(replica4);

            List<FabRole> learnerAndAcceptor = List.of(FabRole.ACCEPTOR, FabRole.LEARNER);
            FabReplica replica5 = new FabReplica("E", nodesIds, transport, null, learnerAndAcceptor, false, timeout, p, a, l, f,
                    new TreeSet<>(List.of("A", "B", "C", "D", "F")),
                    new TreeSet<>(List.of("C", "D", "F")),
                    new TreeSet<>(List.of("A", "B", "C", "D")),
                    "A");
            replicas.add(replica5);
            this.addNode(replica5);

            FabReplica replica6 = new FabReplica("F", nodesIds, transport, null, learnerAndAcceptor, false, timeout, p, a, l, f,
                    new TreeSet<>(List.of("A", "B", "C", "D", "E")),
                    new TreeSet<>(List.of("C", "D", "E")),
                    new TreeSet<>(List.of("A", "B", "C", "D")),
                    "A");
            replicas.add(replica6);
            this.addNode(replica6);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void run() {
        // send a request message to node A
        try {
            this.setNumClients(1);
            this.transport.sendClientRequest("C0", "123", "A");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TerminationCondition getTerminationCondition() {
        return null;
    }
}
