//package byzzbench.simulator.protocols.fab.test;
//
//import static org.mockito.Mockito.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//import byzzbench.simulator.protocols.pbft_java.MessageLog;
//import byzzbench.simulator.protocols.fab.replicas.FabReplica;
//import byzzbench.simulator.protocols.fab.replicas.FabRole;
//import byzzbench.simulator.transport.Transport;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//import java.util.SortedSet;
//import java.util.TreeSet;
//
//public class FabReplicaTest {
//
//    private FabReplica replica;
//    private Transport transport;
//    private MessageLog messageLog;
//    private SortedSet<String> nodeIds;
//    private SortedSet<String> acceptorNodeIds;
//    private SortedSet<String> learnerNodeIds;
//    private SortedSet<String> proposerNodeIds;
//
//    @BeforeEach
//    void setUp() {
//        transport = mock(Transport.class);
//        messageLog = mock(MessageLog.class);
//
//        nodeIds = new TreeSet<>(List.of("A", "B", "C", "D", "E", "F"));
//        acceptorNodeIds = new TreeSet<>(List.of("A", "B", "C", "D", "E", "F"));
//        learnerNodeIds = new TreeSet<>(List.of("C", "D", "E", "F"));
//        proposerNodeIds = new TreeSet<>(List.of("A", "B", "C", "D"));
//
//        replica = new FabReplica(
//                "A",
//                nodeIds,
//                transport,
//                null,
//                messageLog,
//                List.of(FabRole.PROPOSER, FabRole.ACCEPTOR),
//                true,
//                80000L,
//                4,
//                6,
//                4,
//                1,
//                acceptorNodeIds,
//                learnerNodeIds,
//                proposerNodeIds,
//                "A"
//        );
//    }
//
//    @Test
//    void testInitialize(){
//        replica.initialize();
//
//        assertNotNull(replica.getAcceptorsWithAcceptedProposal());
//    }
//
//}
