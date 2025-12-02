package byzzbench.simulator.protocols.pbft_java;

import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.transport.DefaultClientReplyPayload;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.nodes.ClientRequestMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doCallRealMethod;

/**
 * Unit tests for PbftClient behavior:
 * - issues requests with unique identifiers
 * - retransmits on timeout (simulated via retransmitRequest)
 * - upon receiving f+1 matching replies, completes and issues the next request
 */
@DisplayName("PBFT Client behavior")
class PbftClientTest {

    private PbftClient client;          // client under test (with mocked scenario)
    private PbftClient spyClient;       // spy to intercept network sends and randomness

    private List<String> sentRequestIds; // captured request IDs for each send

    @BeforeEach
    void setup() {
        // Build a mocked Scenario with 4 replicas and a mocked Transport
        var scenarioMock = Mockito.mock(byzzbench.simulator.Scenario.class, Mockito.withSettings().lenient());
        var transportMock = Mockito.mock(byzzbench.simulator.transport.Transport.class, Mockito.withSettings().lenient());

        // Minimal stubs used by Client methods during these tests
        // - getReplicas().size() should be 4 (n=4 → f=1 → f+1=2)
        // - getTransport() used only by markRequestAsCompleted (clearTimeout), but we do not assert on it
        var replicasMap = new java.util.TreeMap<String, byzzbench.simulator.nodes.Replica>();
        replicasMap.put("A", null);
        replicasMap.put("B", null);
        replicasMap.put("C", null);
        replicasMap.put("D", null);
        Mockito.when(scenarioMock.getReplicas()).thenReturn(replicasMap);
        Mockito.when(scenarioMock.getTransport()).thenReturn(transportMock);
        Mockito.when(scenarioMock.getRandom()).thenReturn(new java.util.Random(1L));

        // Create the client bound to the mocked scenario
        client = new PbftClient(scenarioMock, "C0");
        spyClient = Mockito.spy(client);

        // Deterministic recipient for sends
        doReturn("A").when(spyClient).getRandomRecipientId();

        // Capture request IDs for each send without actually touching the transport/timeouts
        sentRequestIds = new ArrayList<>();
        doAnswer(invocation -> {
            String requestId = invocation.getArgument(0);
            sentRequestIds.add(requestId);
            return null; // avoid real network/timer side-effects
        }).when(spyClient).sendRequest(anyString(), anyString());

        // Also stub the no-arg sendRequest to route through our capture without timers
        doAnswer(invocation -> {
            String rid = spyClient.generateRequestId();
            // emulate the behavior of choosing a recipient but do not call transport
            String recipient = spyClient.getRandomRecipientId();
            sentRequestIds.add(rid);
            return null;
        }).when(spyClient).sendRequest();
    }

    @Test
    @DisplayName("Retransmission preserves the original timestamp in ClientRequestMessage")
    void retransmit_preservesOriginalTimestamp() {
        // Fresh, dedicated setup for this test (do not use the class-level stubs for sendRequest)
        var scenarioMock = Mockito.mock(byzzbench.simulator.Scenario.class, Mockito.withSettings().lenient());
        var transportMock = Mockito.mock(byzzbench.simulator.transport.Transport.class, Mockito.withSettings().lenient());

        var replicasMap = new java.util.TreeMap<String, byzzbench.simulator.nodes.Replica>();
        replicasMap.put("A", null);
        replicasMap.put("B", null);
        replicasMap.put("C", null);
        replicasMap.put("D", null);
        Mockito.when(scenarioMock.getReplicas()).thenReturn(replicasMap);
        Mockito.when(scenarioMock.getTransport()).thenReturn(transportMock);
        Mockito.when(scenarioMock.getRandom()).thenReturn(new java.util.Random(1L));

        PbftClient c = new PbftClient(scenarioMock, "C0");
        PbftClient s = Mockito.spy(c);

        // Deterministic recipient
        doReturn("A").when(s).getRandomRecipientId();

        // Mock transport behaviors used by sendRequest
        // - Capture payloads sent
        List<MessagePayload> sentPayloads = new ArrayList<>();
        Mockito.doAnswer(inv -> {
            MessagePayload payload = inv.getArgument(1);
            sentPayloads.add(payload);
            return null;
        }).when(transportMock).sendMessage(Mockito.eq(s), Mockito.any(), Mockito.anyString());

        // - Avoid scheduling real timeouts
        Mockito.when(transportMock.setTimeout(Mockito.eq(s), Mockito.any(Runnable.class), Mockito.any(), Mockito.anyString()))
                .thenReturn(1L);

        // Make consecutive calls to getCurrentTime() return increasing values, as in the real system
        // This ensures the test would fail if retransmission did not preserve the original timestamp
        java.time.Instant t1 = java.time.Instant.ofEpochMilli(1000);
        java.time.Instant t2 = java.time.Instant.ofEpochMilli(2000);
        // Important: use doReturn for spies to avoid calling the real method during stubbing
        doReturn(t1, t2).when(s).getCurrentTime();

        // Act: initial send
        s.initialize();
        assertEquals(1, sentPayloads.size(), "First request should be sent on initialize()");
        assertTrue(sentPayloads.get(0) instanceof ClientRequestMessage, "Payload should be a ClientRequestMessage");
        ClientRequestMessage first = (ClientRequestMessage) sentPayloads.get(0);

        // Act: retransmit
        s.retransmitRequest();
        assertEquals(2, sentPayloads.size(), "Retransmission should send another message");
        assertTrue(sentPayloads.get(1) instanceof ClientRequestMessage, "Payload should be a ClientRequestMessage");
        ClientRequestMessage second = (ClientRequestMessage) sentPayloads.get(1);

        // Assert: same requestId and same timestamp
        assertEquals(first.getRequestId(), second.getRequestId(), "Retransmit must keep same requestId");
        assertEquals(first.getTimestamp(), second.getTimestamp(), "Retransmit must preserve the original timestamp");
    }

    @Test
    @DisplayName("Client issues requests with unique sequential identifiers")
    void issuesUniqueSequentialRequestIds() {
        // Act: initialize triggers the first send
        spyClient.initialize();
        assertEquals(1, sentRequestIds.size(), "First request should be sent on initialize()");
        String firstId = sentRequestIds.get(0);
        assertTrue(firstId.matches("C0/\\d+"), "Request ID should follow pattern C0/<seq>");

        // Deliver f+1 (=2 for n=4) matching replies to complete the request
        spyClient.handleMessage("A", new DefaultClientReplyPayload(firstId, "ok"));
        spyClient.handleMessage("B", new DefaultClientReplyPayload(firstId, "ok"));

        // After completion, client should automatically issue the next request
        assertEquals(2, sentRequestIds.size(), "Next request should be auto-issued after completion");
        String secondId = sentRequestIds.get(1);

        // Assert unique and sequential by suffix (increment of 1)
        assertNotEquals(firstId, secondId, "Consecutive request IDs must differ");
        long firstSeq = Long.parseLong(firstId.substring(firstId.indexOf('/') + 1));
        long secondSeq = Long.parseLong(secondId.substring(secondId.indexOf('/') + 1));
        assertEquals(firstSeq + 1, secondSeq, "Second request should increment sequence by 1");
    }

    @Test
    @DisplayName("Client retransmits the same request ID when a timeout occurs (simulated)")
    void retransmitsOnTimeout() {
        // Initialize and capture the first request
        spyClient.initialize();
        assertEquals(1, sentRequestIds.size());
        String firstId = sentRequestIds.get(0);

        // Simulate timeout by calling retransmitRequest (unit-level simulation)
        spyClient.retransmitRequest();

        // A second send should occur with the same requestId
        assertEquals(2, sentRequestIds.size(), "Retransmission should trigger another send");
        String retransmittedId = sentRequestIds.get(1);
        assertEquals(firstId, retransmittedId, "Retransmission must use the same requestId");
    }

    @Test
    @DisplayName("Upon receiving f+1 matching replies (n=4 → 2), request completes and next is issued")
    void completesAfterFPlusOneMatchingReplies_andIssuesNext() {
        // Start first request
        spyClient.initialize();
        assertEquals(1, sentRequestIds.size());
        String reqId = sentRequestIds.get(0);

        // Sanity: f+1 = 2 for n=4 (replicas mocked above)
        assertEquals(4, spyClient.getScenario().getReplicas().size());

        // Send two matching replies from different replicas
        spyClient.handleMessage("A", new DefaultClientReplyPayload(reqId, "result-42"));
        spyClient.handleMessage("B", new DefaultClientReplyPayload(reqId, "result-42"));

        // Assert the request is marked completed
        assertTrue(spyClient.getCompletedRequests().contains(reqId), "Request should be marked as completed");

        // And the client should have issued the next request automatically
        assertEquals(2, sentRequestIds.size(), "Next request should be issued after completion");
        String nextId = sentRequestIds.get(1);
        long seq1 = Long.parseLong(reqId.substring(reqId.indexOf('/') + 1));
        long seq2 = Long.parseLong(nextId.substring(nextId.indexOf('/') + 1));
        assertEquals(seq1 + 1, seq2, "Next request should increment sequence by 1");
    }
}
