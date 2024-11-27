package byzzbench.simulator.protocols.fab;

import byzzbench.simulator.BaseScenario;
import byzzbench.simulator.TerminationCondition;
import byzzbench.simulator.protocols.fab.replicas.FabReplica;
import byzzbench.simulator.protocols.fab.replicas.FabRole;
import byzzbench.simulator.scheduler.Scheduler;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class FabScenario extends BaseScenario {
    private final int NUM_NODES = 6;
    private List<FabReplica> replicas = new ArrayList<>();

    public FabScenario(Scheduler scheduler) {
        super("fab", scheduler);
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
        try {
            SortedSet<String> nodesIds = new TreeSet<>();
            for (int i = 0; i < NUM_NODES; i++) {
                nodesIds.add(Character.toString((char) ('A' + i)));
            }

            List<FabRole> proposerAndAcceptor = List.of(FabRole.PROPOSER, FabRole.ACCEPTOR);
            FabReplica replica1 = new FabReplica("A", nodesIds, transport, null, proposerAndAcceptor, true, 1000, p, a, l, f);
            replicas.add(replica1);

            FabReplica replica2 = new FabReplica("B", nodesIds, transport, null, proposerAndAcceptor, false, 1000, p, a, l, f);
            replicas.add(replica2);

            List<FabRole> proposerAndAcceptorAndLearner = List.of(FabRole.PROPOSER, FabRole.ACCEPTOR, FabRole.LEARNER);

            FabReplica replica3 = new FabReplica("C", nodesIds, transport, null, proposerAndAcceptorAndLearner, false, 1000, p, a, l, f);
            replicas.add(replica3);

            FabReplica replica4 = new FabReplica("D", nodesIds, transport, null, proposerAndAcceptorAndLearner, false, 1000, p, a, l, f);
            replicas.add(replica4);

            List<FabRole> learnerAndAcceptor = List.of(FabRole.ACCEPTOR, FabRole.LEARNER);
            FabReplica replica5 = new FabReplica("E", nodesIds, transport, null, learnerAndAcceptor, false, 1000, p, a, l, f);
            replicas.add(replica5);

            FabReplica replica6 = new FabReplica("F", nodesIds, transport, null, learnerAndAcceptor, false, 1000, p, a, l, f);
            replicas.add(replica6);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void run() {

    }

    @Override
    public TerminationCondition getTerminationCondition() {
        return null;
    }
}
