package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.exploration_strategy.ExplorationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.SortedSet;
import java.util.TreeSet;

@SpringBootTest
public class TendermintReplicaTest {

    private final int tolerance = 1;
    ExplorationStrategy explorationStrategy = Mockito.mock(ExplorationStrategy.class);
    private TendermintReplica replicaA;
    private TendermintReplica replicaB;
    private TendermintReplica replicaC;
    private TendermintReplica replicaD;

    @Test
    void test() {
    }

    @BeforeEach
    void setup() {
        String replicaAId = "A";
        String replicaBId = "B";
        String replicaCId = "C";
        String replicaDId = "D";
        SortedSet<String> nodeIds = new TreeSet<>();
        nodeIds.add("A");
        nodeIds.add("B");
        nodeIds.add("C");
        nodeIds.add("D");

        TendermintScenarioExecutor tendermintScenarioExecutor = new TendermintScenarioExecutor(explorationStrategy);
        replicaA = new TendermintReplica(replicaAId, nodeIds, tendermintScenarioExecutor);
        replicaB = new TendermintReplica(replicaBId, nodeIds, tendermintScenarioExecutor);
        replicaC = new TendermintReplica(replicaCId, nodeIds, tendermintScenarioExecutor);
        replicaD = new TendermintReplica(replicaDId, nodeIds, tendermintScenarioExecutor);

        replicaA.initialize();
        replicaB.initialize();
        replicaC.initialize();
        replicaD.initialize();
    }

    @Test
    void testHandleProposal() {
//        TendermintReplica spyReplica = Mockito.spy(replicaA);
//        RequestMessage request = new RequestMessage("123", 0, "C0");
//        Block block = new Block(1, 1, 1, "Block", null);
//        ProposalMessage proposal = new ProposalMessage("A", 0, 1, spyReplica.getValidRound(), block);
//
//        spyReplica.handleProposal(proposal);
//
//        verify(spyReplica, times(1)).handleProposal(proposal);
    }
}
