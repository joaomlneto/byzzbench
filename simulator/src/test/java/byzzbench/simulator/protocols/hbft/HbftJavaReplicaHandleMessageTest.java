package byzzbench.simulator.protocols.hbft;

import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.protocols.hbft.message.*;
import byzzbench.simulator.transport.MessagePayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.Serializable;
import java.time.Instant;
import java.util.SortedSet;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for HbftJavaReplica message dispatch and basic request handling.
 * <p>
 * These tests focus on per-spec behaviors from hBFT (see: https://fififish.github.io/sisiduan/files/tdsc.pdf):
 * - 4.1 Client request handling and forwarding to the primary
 * - 4.1 Reply resending is left to integration tests; here we verify dispatch only
 * - 4.3 Timeouts are not exercised here to keep tests deterministic
 */
@DisplayName("hBFT: message dispatch and request handling")
public class HbftJavaReplicaHandleMessageTest {

    private HbftJavaReplica<Serializable, Serializable> replica;
    private HbftJavaScenario scenario;

    @BeforeEach
    void setup() {
        // Minimal schedule and scenario
        ScenarioParameters params = ScenarioParameters.builder()
                .scenarioId("hbft-test")
                .randomSeed(42L)
                .numClients(1)
                .numReplicas(4)
                .build();
        Schedule schedule = new Schedule(params);

        scenario = new HbftJavaScenario(schedule);
        SortedSet<String> nodeIds = scenario.getNodeIds();

        MessageLog log = new MessageLog(100, 100, 200);
        HbftJavaReplica<Serializable, Serializable> base =
                new HbftJavaReplica<>("A", nodeIds, 1, 2, log, scenario);

        // Spy so we can verify dispatch without executing heavy internals
        replica = Mockito.spy(base);

        // Default: make network-affecting methods no-op
        doNothing().when(replica).broadcastMessage(any());
        doNothing().when(replica).sendReply(anyString(), any());
        doNothing().when(replica).sendRequest(anyString(), any());

        // For pure dispatch tests, stub handler methods to no-op
        doNothing().when(replica).handleClientRequest(anyString(), any());
        doNothing().when(replica).recvRequest(any());
        doNothing().when(replica).recvPrepare(any());
        doNothing().when(replica).recvCommit(any());
        doNothing().when(replica).recvCheckpoint(any());
        doNothing().when(replica).recvViewChange(any());
        doNothing().when(replica).recvNewView(any());
        doNothing().when(replica).recvPanic(any());

        // Initialize so that view/primary are well-defined (view=1 => primary is "B" for A,B,C,D)
        replica.initialize();
    }

    @Test
    @DisplayName("Dispatches ClientRequestMessage to handleClientRequest")
    void dispatchesClientRequestMessage() {
        ClientRequestMessage crm = Mockito.mock(ClientRequestMessage.class);
        replica.handleMessage("client-1", crm);
        verify(replica, times(1)).handleClientRequest(eq("client-1"), same(crm));
    }

    @Test
    @DisplayName("Dispatches RequestMessage to recvRequest")
    void dispatchesRequestMessage() {
        RequestMessage req = Mockito.mock(RequestMessage.class);
        replica.handleMessage("sender-X", req);
        verify(replica, times(1)).recvRequest(same(req));
    }

    @Test
    @DisplayName("Dispatches PrepareMessage to recvPrepare")
    void dispatchesPrepareMessage() {
        PrepareMessage m = Mockito.mock(PrepareMessage.class);
        replica.handleMessage("B", m);
        verify(replica, times(1)).recvPrepare(same(m));
    }

    @Test
    @DisplayName("Dispatches CommitMessage to recvCommit")
    void dispatchesCommitMessage() {
        CommitMessage m = Mockito.mock(CommitMessage.class);
        replica.handleMessage("C", m);
        verify(replica, times(1)).recvCommit(same(m));
    }

    @Test
    @DisplayName("Dispatches CheckpointMessage to recvCheckpoint")
    void dispatchesCheckpointMessage() {
        CheckpointMessage m = Mockito.mock(CheckpointIMessage.class);
        replica.handleMessage("B", m);
        verify(replica, times(1)).recvCheckpoint(same(m));
    }

    @Test
    @DisplayName("Dispatches ViewChangeMessage to recvViewChange")
    void dispatchesViewChangeMessage() {
        ViewChangeMessage m = Mockito.mock(ViewChangeMessage.class);
        replica.handleMessage("B", m);
        verify(replica, times(1)).recvViewChange(same(m));
    }

    @Test
    @DisplayName("Dispatches NewViewMessage to recvNewView")
    void dispatchesNewViewMessage() {
        NewViewMessage m = Mockito.mock(NewViewMessage.class);
        replica.handleMessage("B", m);
        verify(replica, times(1)).recvNewView(same(m));
    }

    @Test
    @DisplayName("Dispatches PanicMessage to recvPanic")
    void dispatchesPanicMessage() {
        PanicMessage m = Mockito.mock(PanicMessage.class);
        replica.handleMessage("client-1", m);
        verify(replica, times(1)).recvPanic(same(m));
    }

    @Test
    @DisplayName("Throws on null or unknown payload type")
    void throwsOnNullOrUnknownPayload() {
        assertThrows(RuntimeException.class, () -> replica.handleMessage("any", null));
        assertThrows(RuntimeException.class, () -> replica.handleMessage("any", new DummyPayload()));
    }

    @Test
    @DisplayName("Non-primary forwards request once to primary; duplicate ignored within view")
    void nonPrimaryForwardsRequestToPrimary_andDuplicatesIgnoredWithinView() {
        // Reconfigure spy to use real recvRequest logic for this test
        // but keep network calls as no-op to avoid touching transport
        reset(replica);

        doCallRealMethod().when(replica).initialize();
        doCallRealMethod().when(replica).handleMessage(anyString(), any());
        doCallRealMethod().when(replica).recvRequest(any());
        try {
            HbftJavaReplica.class.getDeclaredMethod("recvRequest", RequestMessage.class, boolean.class)
                    .setAccessible(true);
        } catch (Exception ignored) {
        }

        doNothing().when(replica).broadcastMessage(any());
        doNothing().when(replica).sendReply(anyString(), any());
        doNothing().when(replica).sendRequest(anyString(), any());

        replica.initialize(); // sets view=1 -> primary is "B"

        // A receives a client request; as non-primary, it should forward to B once
        RequestMessage req = new RequestMessage("op-1", Instant.ofEpochMilli(123), "C0");
        replica.recvRequest(req);
        // Duplicate in same view should be ignored
        replica.recvRequest(req);

        // Verify forwarding only once
        verify(replica, times(1)).sendRequest(eq("B"), same(req));

        // Ensure duplicate did not trigger a second forward
        verify(replica, times(1)).sendRequest(eq("B"), same(req));
    }

    /**
     * Minimal dummy payload to exercise the default branch (unknown type).
     */
    static class DummyPayload extends MessagePayload {
        @Override
        public String getType() {
            return "DUMMY";
        }
    }
}
