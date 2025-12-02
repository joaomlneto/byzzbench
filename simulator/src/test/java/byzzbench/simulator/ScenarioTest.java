package byzzbench.simulator;

import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.nodes.Client;
import byzzbench.simulator.nodes.ClientReply;
import byzzbench.simulator.nodes.Node;
import byzzbench.simulator.nodes.Replica;
import byzzbench.simulator.state.CommitLog;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.Transport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the core Scenario behavior.
 */
@SpringBootTest
@DisplayName("Scenario Tests")
class ScenarioTest {

    // ---- Test scaffolding ----

    private TestScenario scenario;

    @BeforeEach
    void setUp() {
        ScenarioParameters params = ScenarioParameters.builder()
                .randomSeed(1L)
                .build();
        Schedule schedule = new Schedule(params);
        scenario = new TestScenario(schedule);
    }

    @Test
    @DisplayName("runScenario invokes run implementation")
    void runScenario_invokesRunImplementation() {
        assertFalse(scenario.runCalled.get());
        scenario.runScenario();
        assertTrue(scenario.runCalled.get());
    }

    @Test
    @DisplayName("markReplicaFaulty and isFaultyReplica work as expected")
    void markAndCheckFaultyReplica() {
        assertFalse(scenario.isFaultyReplica("r1"));
        scenario.markReplicaFaulty("r1");
        assertTrue(scenario.isFaultyReplica("r1"));
    }

    @Test
    @DisplayName("addClient adds to nodes and notifies observers")
    void addClient_addsToNodesAndNotifiesObservers() {
        RecordingObserver observer = new RecordingObserver();
        scenario.addObserver(observer);

        DummyClient c1 = new DummyClient("c1", scenario);
        scenario.addClient(c1);

        // nodes map contains client
        NavigableMap<String, Node> nodes = scenario.getNodes();
        assertTrue(nodes.containsKey("c1"));
        assertSame(c1, nodes.get("c1"));

        // getClients returns client
        assertEquals(1, scenario.getClients().size());
        assertSame(c1, scenario.getClients().get("c1"));

        // observer notified
        assertEquals(List.of("c1"), observer.clientsAdded);
    }

    @Test
    @DisplayName("addNode adds to nodes, registers network faults, and notifies observers")
    void addNode_addsToNodes_registersNetworkFaults_andNotifiesObservers() {
        RecordingObserver observer = new RecordingObserver();
        scenario.addObserver(observer);

        Transport transport = scenario.getTransport();
        int faultsBefore = transport.getEnabledNetworkFaults().size();

        DummyReplica r1 = new DummyReplica("r1", scenario, new TotalOrderCommitLog());
        scenario.addNode(r1);

        // nodes map contains replica
        NavigableMap<String, Node> nodes = scenario.getNodes();
        assertTrue(nodes.containsKey("r1"));
        assertSame(r1, nodes.get("r1"));

        // getReplicas returns replica
        assertEquals(1, scenario.getReplicas().size());
        assertSame(r1, scenario.getReplicas().get("r1"));

        // two faults were registered (IsolateProcessNetworkFault + HealNodeNetworkFault)
        int faultsAfter = transport.getEnabledNetworkFaults().size();
        assertTrue(faultsAfter >= faultsBefore, "Network faults list should be queryable");
        // We cannot assert exact count of enabled faults because predicates may disable them by default,
        // but we can assert that the faults exist in the transport registry by their IDs.
        assertNotNull(transport.getNetworkFault("IsolateProcessNetworkFault-r1"));
        assertNotNull(transport.getNetworkFault("HealProcessNetworkFault(r1)"));

        // observer notified
        assertEquals(List.of("r1"), observer.replicasAdded);
    }

    @Test
    @DisplayName("getNode returns previously added node or null")
    void getNode_returnsPreviouslyAddedNodeOrNull() {
        assertNull(scenario.getNode("missing"));
        DummyClient c1 = new DummyClient("c1", scenario);
        scenario.addClient(c1);
        assertSame(c1, scenario.getNode("c1"));
    }

    /**
     * Minimal concrete Scenario for testing; run() is a no-op but we track invocation.
     */
    static class TestScenario extends Scenario {
        final AtomicBoolean runCalled = new AtomicBoolean(false);

        protected TestScenario(Schedule schedule) {
            super(schedule);
        }

        @Override
        protected void run() {
            runCalled.set(true);
        }

        @Override
        protected void loadScenarioParameters(byzzbench.simulator.domain.ScenarioParameters parameters) { /* no-op for tests */ }

        @Override
        protected void setup() { /* no-op for tests */ }

        @Override
        public int maxFaultyReplicas(int n) {
            return n <= 0 ? 0 : Math.max(0, (n - 1) / 3);
        }

        @Override
        public Class<? extends Replica> getReplicaClass() {
            return DummyReplica.class;
        }

        @Override
        public Class<? extends Client> getClientClass() {
            return DummyClient.class;
        }
    }

    /**
     * Minimal Replica implementation for tests with no-op message handling.
     */
    static class DummyReplica extends Replica {
        protected DummyReplica(String id, Scenario scenario, CommitLog commitLog) {
            super(id, scenario, commitLog);
        }

        @Override
        public void handleMessage(String sender, byzzbench.simulator.transport.MessagePayload message) {
            // no-op
        }
    }

    /**
     * Minimal Client implementation for tests; never marks request completed automatically.
     */
    static class DummyClient extends Client {
        public DummyClient(String id, Scenario scenario) {
            super(scenario, id);
        }

        @Override
        public boolean isRequestCompleted(ClientReply message) {
            return false;
        }

        @Override
        public void handleMessage(String sender, byzzbench.simulator.transport.MessagePayload message) {
            // no-op
        }
    }

    /**
     * Observer that records notifications for assertions.
     */
    static class RecordingObserver implements ScenarioObserver {
        final List<String> replicasAdded = new ArrayList<>();
        final List<String> clientsAdded = new ArrayList<>();

        @Override
        public void onReplicaAdded(Replica replica) {
            replicasAdded.add(replica.getId());
        }

        @Override
        public void onClientAdded(Client client) {
            clientsAdded.add(client.getId());
        }
    }
}
