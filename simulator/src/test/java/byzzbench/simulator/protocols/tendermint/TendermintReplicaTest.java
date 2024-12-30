package byzzbench.simulator.protocols.tendermint;

import java.util.SortedSet;
import java.util.TreeSet;

import byzzbench.simulator.protocols.tendermint.message.Block;
import byzzbench.simulator.protocols.tendermint.message.ProposalMessage;
import byzzbench.simulator.protocols.tendermint.message.RequestMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.springframework.boot.test.context.SpringBootTest;

import byzzbench.simulator.scheduler.Scheduler;

@SpringBootTest
public class TendermintReplicaTest {

    private TendermintReplica replicaA;
    private TendermintReplica replicaB;
    private TendermintReplica replicaC;
    private TendermintReplica replicaD;
    private int tolerance = 1;
    Scheduler scheduler = Mockito.mock(Scheduler.class);

    @Test
    void test() {}

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

        TendermintScenarioExecutor tendermintScenarioExecutor = new TendermintScenarioExecutor(scheduler);
        replicaA = new TendermintReplica(replicaAId, nodeIds, tendermintScenarioExecutor);
        replicaB = new TendermintReplica(replicaBId, nodeIds, tendermintScenarioExecutor);
        replicaC = new TendermintReplica(replicaCId, nodeIds, tendermintScenarioExecutor);
        replicaD = new TendermintReplica(replicaDId, nodeIds, tendermintScenarioExecutor);

        // Sets the viewNumber to 1 for the replicaA
        replicaA.initialize();
        replicaB.initialize();
        replicaC.initialize();
        replicaD.initialize();
    }

    @Test
    void testHandleProposal() {
        TendermintReplica spyReplica = Mockito.spy(replicaA);
        RequestMessage request = new RequestMessage("123", 0, "C0");
        Block block = new Block(1, 1, 1, "Block", null);
        ProposalMessage proposal = new ProposalMessage("A", 0, 1, spyReplica.getValidRound(), block);

        spyReplica.handleProposal(proposal);

        verify(spyReplica, times(1)).handleProposal(proposal);
    }
}
