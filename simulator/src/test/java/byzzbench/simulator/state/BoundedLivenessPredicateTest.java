package byzzbench.simulator.state;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.nodes.Client;
import byzzbench.simulator.nodes.Replica;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MutateMessageEventPayload;
import byzzbench.simulator.transport.TimeoutEvent;
import byzzbench.simulator.transport.Transport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("BoundedLivenessPredicate Tests")
class BoundedLivenessPredicateTest {

    @Mock
    private Scenario mockScenario;

    @Mock
    private Transport mockTransport;

    @Mock
    private List<Replica> mockReplicas;

    @Mock
    private Schedule mockSchedule;

    private BoundedLivenessPredicate predicate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup default mock behavior
        when(mockScenario.getTransport()).thenReturn(mockTransport);
        when(mockScenario.getSchedule()).thenReturn(mockSchedule);

        NavigableMap<String, Replica> replicasMap = new TreeMap<>();
        for (int i = 0; i < 4; i++) {
            Replica mockReplica = mock(Replica.class);
            replicasMap.put("replica" + i, mockReplica);
        }
        when(mockScenario.getReplicas()).thenReturn(replicasMap);

        // Default schedule length
        when(mockSchedule.getLength()).thenReturn(0);

        predicate = new BoundedLivenessPredicate(mockScenario);
    }

    @Test
    @DisplayName("Should return true before GST is reached")
    void testBeforeGst() {
        boolean result = predicate.test(mockScenario);

        assertTrue(result);
        assertEquals("We are before GST", predicate.getExplanation());
    }

    @Test
    @DisplayName("Should return 0 events since GST when GST not reached")
    void testEventsSinceGstBeforeGst() {
        long eventsSinceGst = predicate.eventsSinceGst(mockScenario);

        assertEquals(0, eventsSinceGst);
    }

    @Test
    @DisplayName("Should track GST when onGlobalStabilizationTime is called")
    void testOnGlobalStabilizationTime() {
        when(mockSchedule.getLength()).thenReturn(5);

        predicate.onGlobalStabilizationTime();

        // After GST, eventsSinceGst should be calculated
        when(mockSchedule.getLength()).thenReturn(8);
        long eventsSinceGst = predicate.eventsSinceGst(mockScenario);

        assertEquals(3, eventsSinceGst);
    }

    @Test
    @DisplayName("Should return true when value committed after GST")
    void testCommitAfterGst() {
        // Trigger GST
        when(mockSchedule.getLength()).thenReturn(5);
        predicate.onGlobalStabilizationTime();

        // Commit a value
        when(mockSchedule.getLength()).thenReturn(7);
        predicate.onLocalCommit(mockReplicas.getFirst(), "test-value");

        boolean result = predicate.test(mockScenario);

        assertTrue(result);
        assertEquals("A value has been committed after GST", predicate.getExplanation());
    }

    @Test
    @DisplayName("Should ignore commits before GST")
    void testCommitBeforeGst() {
        // Commit before GST
        predicate.onLocalCommit(mockReplicas.getFirst(), "test-value");

        // Trigger GST
        when(mockSchedule.getLength()).thenReturn(5);
        predicate.onGlobalStabilizationTime();

        // Should still be in grace period, not marked as committed
        when(mockSchedule.getLength()).thenReturn(7);
        boolean result = predicate.test(mockScenario);

        assertTrue(result);
        assertTrue(predicate.getExplanation().contains("Grace period"));
    }

    @Test
    @DisplayName("Should return true within grace period after GST")
    void testWithinGracePeriod() {
        // Trigger GST at event 5
        when(mockSchedule.getLength()).thenReturn(5);
        predicate.onGlobalStabilizationTime();

        // Move to event 10 (5 events after GST, within LENGTH of 10)
        when(mockSchedule.getLength()).thenReturn(10);
        boolean result = predicate.test(mockScenario);

        assertTrue(result);
        assertTrue(predicate.getExplanation().contains("Grace period: 5 events since GST"));
    }

    @Test
    @DisplayName("Should return true at the boundary of grace period")
    void testAtGracePeriodBoundary() {
        // Trigger GST at event 5
        when(mockSchedule.getLength()).thenReturn(5);
        predicate.onGlobalStabilizationTime();

        // Move to event 15 (exactly 10 events after GST, at LENGTH boundary)
        when(mockSchedule.getLength()).thenReturn(15);
        boolean result = predicate.test(mockScenario);

        assertTrue(result);
        assertTrue(predicate.getExplanation().contains("Grace period: 10 events since GST"));
    }

    @Test
    @DisplayName("Should return false when exceeding grace period without commit")
    void testExceedGracePeriod() {
        // Trigger GST at event 5
        when(mockSchedule.getLength()).thenReturn(5);
        predicate.onGlobalStabilizationTime();

        // Move to event 16 (11 events after GST, exceeding LENGTH of 10)
        when(mockSchedule.getLength()).thenReturn(16);
        boolean result = predicate.test(mockScenario);

        assertFalse(result);
        assertTrue(predicate.getExplanation().contains("Liveness violated: 11 events since GST"));
    }

    @Test
    @DisplayName("Should return false when significantly exceeding grace period")
    void testSignificantlyExceedGracePeriod() {
        // Trigger GST at event 10
        when(mockSchedule.getLength()).thenReturn(10);
        predicate.onGlobalStabilizationTime();

        // Move to event 50 (40 events after GST)
        when(mockSchedule.getLength()).thenReturn(50);
        boolean result = predicate.test(mockScenario);

        assertFalse(result);
        assertTrue(predicate.getExplanation().contains("Liveness violated: 40 events since GST"));
        assertTrue(predicate.getExplanation().contains("max allowed: 10"));
    }

    @Test
    @DisplayName("Should remain true after commit even with many events")
    void testCommitMaintainsLiveness() {
        // Trigger GST
        when(mockSchedule.getLength()).thenReturn(5);
        predicate.onGlobalStabilizationTime();

        // Commit a value after GST
        when(mockSchedule.getLength()).thenReturn(7);
        predicate.onLocalCommit(mockReplicas.getFirst(), "test-value");

        // Move far ahead in the schedule
        when(mockSchedule.getLength()).thenReturn(100);
        boolean result = predicate.test(mockScenario);

        assertTrue(result);
        assertEquals("A value has been committed after GST", predicate.getExplanation());
    }

    @Test
    @DisplayName("Should register observers on construction")
    void testObserverRegistration() {
        verify(mockScenario).addObserver(predicate);
        verify(mockTransport).addObserver(predicate);
        for (Replica mockReplica : mockScenario.getReplicas().values()) {
            verify(mockReplica).addObserver(predicate);
        }
    }

    @Test
    @DisplayName("Should register observer when replica is added")
    void testOnReplicaAdded() {
        Replica newReplica = mock(Replica.class);

        predicate.onReplicaAdded(newReplica);

        verify(newReplica).addObserver(predicate);
    }

    @Test
    @DisplayName("Should handle onLeaderChange without errors")
    void testOnLeaderChange() {
        assertDoesNotThrow(() -> predicate.onLeaderChange(mockReplicas.getFirst(), "newLeader"));
    }

    @Test
    @DisplayName("Should handle onTimeout without errors")
    void testOnTimeout() {
        assertDoesNotThrow(() -> predicate.onTimeout(mockReplicas.getFirst()));
    }

    @Test
    @DisplayName("Should handle onClientAdded without errors")
    void testOnClientAdded() {
        Client mockClient = mock(Client.class);
        assertDoesNotThrow(() -> predicate.onClientAdded(mockClient));
    }

    @Test
    @DisplayName("Should handle onEventAdded without errors")
    void testOnEventAdded() {
        Event mockEvent = mock(Event.class);
        assertDoesNotThrow(() -> predicate.onEventAdded(mockEvent));
    }

    @Test
    @DisplayName("Should handle onEventDropped without errors")
    void testOnEventDropped() {
        Event mockEvent = mock(Event.class);
        assertDoesNotThrow(() -> predicate.onEventDropped(mockEvent));
    }

    @Test
    @DisplayName("Should handle onEventRequeued without errors")
    void testOnEventRequeued() {
        Event mockEvent = mock(Event.class);
        assertDoesNotThrow(() -> predicate.onEventRequeued(mockEvent));
    }

    @Test
    @DisplayName("Should handle onEventDelivered without errors")
    void testOnEventDelivered() {
        Event mockEvent = mock(Event.class);
        assertDoesNotThrow(() -> predicate.onEventDelivered(mockEvent));
    }

    @Test
    @DisplayName("Should handle onMessageMutation without errors")
    void testOnMessageMutation() {
        MutateMessageEventPayload mockPayload = mock(MutateMessageEventPayload.class);
        assertDoesNotThrow(() -> predicate.onMessageMutation(mockPayload));
    }

    @Test
    @DisplayName("Should handle onTimeout event without errors")
    void testOnTimeoutEvent() {
        TimeoutEvent mockEvent = mock(TimeoutEvent.class);
        assertDoesNotThrow(() -> predicate.onTimeout(mockEvent));
    }

    @Test
    @DisplayName("Should test scenario when getting explanation")
    void testGetExplanationCallsTest() {
        // Before GST
        String explanation = predicate.getExplanation();

        assertNotNull(explanation);
        assertEquals("We are before GST", explanation);
    }

    @Test
    @DisplayName("Should handle multiple commits after GST")
    void testMultipleCommitsAfterGst() {
        // Trigger GST
        when(mockSchedule.getLength()).thenReturn(5);
        predicate.onGlobalStabilizationTime();

        // First commit
        when(mockSchedule.getLength()).thenReturn(7);
        predicate.onLocalCommit(mockReplicas.getFirst(), "value1");

        assertTrue(predicate.test(mockScenario));

        // Second commit
        when(mockSchedule.getLength()).thenReturn(10);
        predicate.onLocalCommit(mockReplicas.getFirst(), "value2");

        // Should still be true
        assertTrue(predicate.test(mockScenario));
        assertEquals("A value has been committed after GST", predicate.getExplanation());
    }

    @Test
    @DisplayName("Should handle GST at event index 0")
    void testGstAtIndexZero() {
        when(mockSchedule.getLength()).thenReturn(0);
        predicate.onGlobalStabilizationTime();

        when(mockSchedule.getLength()).thenReturn(5);
        long eventsSinceGst = predicate.eventsSinceGst(mockScenario);

        assertEquals(5, eventsSinceGst);
    }

    @Test
    @DisplayName("Should correctly calculate events since GST")
    void testEventsSinceGstCalculation() {
        // GST at event 100
        when(mockSchedule.getLength()).thenReturn(100);
        predicate.onGlobalStabilizationTime();

        // Current event 125
        when(mockSchedule.getLength()).thenReturn(125);
        long eventsSinceGst = predicate.eventsSinceGst(mockScenario);

        assertEquals(25, eventsSinceGst);
    }
}
