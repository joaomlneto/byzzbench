package byzzbench.simulator.protocols.pbft_java;

import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.protocols.hbft.message.ClientRequestMessage;
import byzzbench.simulator.protocols.pbft_java.message.*;
import byzzbench.simulator.transport.MessagePayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for PbftJavaReplica.handleMessage() ensuring messages are
 * dispatched to the correct handler methods following PBFT roles.
 */
@DisplayName("PBFT: handleMessage dispatch and request processing")
public class PbftJavaReplicaHandleMessageTest {

    /**
     * Minimal dummy payload to exercise the default branch.
     */
    static class DummyPayload extends MessagePayload {
        @Override
        public String getType() { return "DUMMY"; }
    }

    private PbftJavaReplica<Serializable, Serializable> replica;
    private MessageLog messageLog;

    @BeforeEach
    void setup() {
        // Minimal schedule + scenario objects just to satisfy constructor deps
        ScenarioParameters params = ScenarioParameters.builder()
                .scenarioId("test")
                .randomSeed(123L)
                .numClients(1)
                .numReplicas(4)
                .build();
        Schedule schedule = new Schedule(params);

        PbftJavaScenario scenario = new PbftJavaScenario(schedule);

        messageLog = Mockito.mock(MessageLog.class);
        PbftJavaReplica<Serializable, Serializable> base =
                new PbftJavaReplica<>("A", scenario, 1, Duration.ofMillis(100), messageLog);

        // Spy so we can verify dispatch without executing heavy internals
        replica = Mockito.spy(base);

        // Make handler methods no-op to avoid touching transport/state
        doNothing().when(replica).handleClientRequest(anyString(), any());
        // default: do not run real recvRequest in generic dispatch tests
        doNothing().when(replica).recvRequest(any());
        doNothing().when(replica).recvPrePrepare(any());
        doNothing().when(replica).recvPrepare(any());
        doNothing().when(replica).recvCommit(any());
        doNothing().when(replica).recvViewChange(any());
        doNothing().when(replica).recvNewView(any());

        // Ensure network ops don't actually send
        doNothing().when(replica).broadcastMessage(any());
        doNothing().when(replica).sendMessage(any(), anyString());

        // Initialize so view number etc. are sane if anything checks them
        replica.initialize();
    }

    @Test
    @DisplayName("Dispatches ClientRequestMessage to handleClientRequest")
    void dispatchesClientRequestMessage() {
        ClientRequestMessage crm = Mockito.mock(ClientRequestMessage.class);
        when(crm.getOperation()).thenReturn("op");
        replica.handleMessage("client-1", crm);
        verify(replica, times(1)).handleClientRequest(eq("client-1"), eq("op"));
    }

    @Test
    @DisplayName("Dispatches RequestMessage to recvRequest")
    void dispatchesRequestMessage() {
        RequestMessage req = Mockito.mock(RequestMessage.class);
        replica.handleMessage("sender-X", req);
        verify(replica, times(1)).recvRequest(eq(req));
    }

    @Test
    @DisplayName("Dispatches PrePrepareMessage to recvPrePrepare")
    void dispatchesPrePrepareMessage() {
        PrePrepareMessage pp = Mockito.mock(PrePrepareMessage.class);
        replica.handleMessage("B", pp);
        verify(replica, times(1)).recvPrePrepare(eq(pp));
    }

    @Test
    @DisplayName("Dispatches PrepareMessage to recvPrepare")
    void dispatchesPrepareMessage() {
        PrepareMessage prepare = Mockito.mock(PrepareMessage.class);
        replica.handleMessage("C", prepare);
        verify(replica, times(1)).recvPrepare(eq(prepare));
    }

    @Test
    @DisplayName("Dispatches CommitMessage to recvCommit")
    void dispatchesCommitMessage() {
        CommitMessage commit = Mockito.mock(CommitMessage.class);
        replica.handleMessage("D", commit);
        verify(replica, times(1)).recvCommit(eq(commit));
    }

    @Test
    @DisplayName("Dispatches ViewChangeMessage to recvViewChange")
    void dispatchesViewChangeMessage() {
        ViewChangeMessage vc = Mockito.mock(ViewChangeMessage.class);
        replica.handleMessage("B", vc);
        verify(replica, times(1)).recvViewChange(eq(vc));
    }

    @Test
    @DisplayName("Dispatches NewViewMessage to recvNewView")
    void dispatchesNewViewMessage() {
        NewViewMessage nv = Mockito.mock(NewViewMessage.class);
        replica.handleMessage("B", nv);
        verify(replica, times(1)).recvNewView(eq(nv));
    }

    @Test
    @DisplayName("Throws on null or unknown payload type")
    void throwsOnNullOrUnknownPayload() {
        assertThrows(IllegalArgumentException.class, () -> replica.handleMessage("any", null));
        assertThrows(IllegalArgumentException.class, () -> replica.handleMessage("any", new DummyPayload()));
    }

    // ================= Additional tests for RequestMessage logic/state =================

    @Test
    @DisplayName("Cached ticket: resends reply; no timer and no new ticket")
    void requestWithCachedTicket_resendsReply_andDoesNotStartTimerOrCreateTicket() {
        // Arrange
        Instant ts = Instant.now();
        RequestMessage req = new RequestMessage("op", ts, "C0");

        // For this test, execute the real logic of recvRequest
        doCallRealMethod().when(replica).recvRequest(any());

        // Cached ticket with a completed result
        @SuppressWarnings("unchecked")
        Ticket<Serializable, Serializable> cached = Mockito.mock(Ticket.class);
        when(cached.getViewNumber()).thenReturn(1L);
        when(cached.getRequest()).thenReturn(req);
        CompletableFuture<Serializable> done = CompletableFuture.completedFuture("result");
        when(cached.getResult()).thenReturn(done);
        when(messageLog.getTicketFromCache(any())).thenReturn(cached);

        // We spy on sendReply to verify resend
        doNothing().when(replica).sendReply(anyString(), any(), any());

        // Act
        replica.recvRequest(req);

        // Assert
        verify(replica, times(1)).sendReply(eq("C0"), eq(ts), any(ReplyMessage.class));
        verify(messageLog, never()).newTicket(anyLong(), anyLong());
        verify(replica, never()).sendRequest(anyString(), any());
        verify(replica, never()).broadcastMessage(any());

        // No timers should have been started
        assertTrue(replica.activeTimers().isEmpty());
    }

    @Test
    @DisplayName("Non-primary: starts timer and forwards request to primary only")
    void nonPrimary_receivesRequest_startsTimer_andForwardsToPrimary_only() {
        // Arrange
        Instant ts = Instant.now();
        RequestMessage req = new RequestMessage("op-2", ts, "C1");

        // Execute real logic
        doCallRealMethod().when(replica).recvRequest(any());

        when(messageLog.getTicketFromCache(any())).thenReturn(null);
        when(messageLog.shouldBuffer()).thenReturn(false);

        // Force round-robin primary to be B (non-self)
        doReturn("B").when(replica).getRoundRobinPrimaryId();

        // Monitor forwarding method
        doNothing().when(replica).sendRequest(anyString(), any());

        // Act
        replica.recvRequest(req);

        // Assert: forwarded to primary
        verify(replica, times(1)).sendRequest(eq("B"), eq(req));

        // No ticket creation or broadcast when not primary
        verify(messageLog, never()).newTicket(anyLong(), anyLong());
        verify(replica, never()).broadcastMessage(any());

        // Timer should be started for this request
        assertEquals(1, replica.activeTimers().size());
    }

    @Test
    @DisplayName("Primary: starts timer, creates ticket, broadcasts PrePrepare, and appends")
    void primary_processesRequest_startsTimer_createsTicket_broadcastsPrePrepare_andAppendsToTicket() {
        // Arrange
        Instant ts = Instant.now();
        RequestMessage req = new RequestMessage("op-3", ts, "C2");

        // Execute real logic
        doCallRealMethod().when(replica).recvRequest(any());

        when(messageLog.getTicketFromCache(any())).thenReturn(null);
        when(messageLog.shouldBuffer()).thenReturn(false);

        // Self is the primary
        doReturn("A").when(replica).getRoundRobinPrimaryId();

        // Stub digest to a known value for verification
        byte[] digest = new byte[]{1, 2, 3};
        doReturn(digest).when(replica).digest(any());

        // Mock ticket returned by newTicket
        @SuppressWarnings("unchecked")
        Ticket<Serializable, Serializable> ticket = Mockito.mock(Ticket.class);
        when(messageLog.newTicket(anyLong(), anyLong())).thenReturn(ticket);

        // Capture broadcasted message implicitly via verify
        doNothing().when(replica).broadcastMessage(any());

        // Act
        replica.recvRequest(req);

        // Assert: timer started
        assertEquals(1, replica.activeTimers().size());

        // Ticket created for current view and sequence
        verify(messageLog, times(1)).newTicket(eq(replica.getViewNumber()), anyLong());

        // Request appended to ticket
        verify(ticket, times(1)).append(eq(req));

        // PrePrepare broadcast and appended
        verify(replica, times(1)).broadcastMessage(argThat(msg -> {
            if (!(msg instanceof PrePrepareMessage pp)) return false;
            return pp.getViewNumber() == replica.getViewNumber()
                    && pp.getRequest().equals(req)
                    && java.util.Arrays.equals(pp.getDigest(), digest);
        }));

        verify(ticket, times(1)).append(argThat(arg -> arg instanceof PrePrepareMessage));
    }
}
