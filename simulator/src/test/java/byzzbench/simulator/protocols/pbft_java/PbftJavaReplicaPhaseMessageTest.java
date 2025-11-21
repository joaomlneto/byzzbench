package byzzbench.simulator.protocols.pbft_java;

import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.protocols.pbft_java.message.CommitMessage;
import byzzbench.simulator.protocols.pbft_java.message.PrePrepareMessage;
import byzzbench.simulator.protocols.pbft_java.message.PrepareMessage;
import byzzbench.simulator.protocols.pbft_java.message.RequestMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests covering PBFT phase messages handling (PrePrepare/Prepare/Commit)
 * for PbftJavaReplica, focusing on verification rules and actions.
 */
public class PbftJavaReplicaPhaseMessageTest {

    private PbftJavaReplica<Serializable, Serializable> replica;
    private MessageLog messageLog;

    @BeforeEach
    void setup() {
        ScenarioParameters params = ScenarioParameters.builder()
                .scenarioId("test")
                .randomSeed(42L)
                .numClients(1)
                .numReplicas(4)
                .build();
        Schedule schedule = new Schedule(params);
        PbftJavaScenario scenario = new PbftJavaScenario(schedule);

        messageLog = Mockito.mock(MessageLog.class);
        PbftJavaReplica<Serializable, Serializable> base =
                new PbftJavaReplica<>("A", scenario, 1, Duration.ofMillis(100), messageLog);
        replica = Mockito.spy(base);

        // Network operations should be no-ops in tests
        doNothing().when(replica).broadcastMessage(any());
        doNothing().when(replica).sendMessage(any(), anyString());

        // Initialize view
        replica.initialize();
    }

    // 1) disgruntled: rejects all PrePrepare/Prepare/Commit
    @Test
    void disgruntledReplica_rejectsAllPhaseMessages() {
        replica.setDisgruntled(true);

        // Enable real handlers
        doCallRealMethod().when(replica).recvPrePrepare(any());
        doCallRealMethod().when(replica).recvPrepare(any());
        doCallRealMethod().when(replica).recvCommit(any());

        // Common values
        long view = replica.getViewNumber();
        long seq = 1L;
        byte[] digest = new byte[]{9, 9, 9};
        RequestMessage req = new RequestMessage("op", Instant.now(), "C0");

        PrePrepareMessage pp = new PrePrepareMessage(view, seq, digest, req);
        PrepareMessage prepare = new PrepareMessage(view, seq, digest, "B");
        CommitMessage commit = new CommitMessage(view, seq, digest, "C");

        // Act
        replica.recvPrePrepare(pp);
        replica.recvPrepare(prepare);
        replica.recvCommit(commit);

        // Assert: no messageLog interaction and no broadcast/append
        verify(messageLog, never()).newTicket(anyLong(), anyLong());
        verify(messageLog, never()).getTicket(anyLong(), anyLong());
        verify(replica, never()).broadcastMessage(any());
    }

    // 2) reject when view number mismatches
    @Test
    void rejectsPhaseMessages_withMismatchedView() {
        doCallRealMethod().when(replica).recvPrePrepare(any());
        doCallRealMethod().when(replica).recvPrepare(any());
        doCallRealMethod().when(replica).recvCommit(any());

        long wrongView = replica.getViewNumber() + 1; // mismatch
        long seq = 2L;
        byte[] digest = new byte[]{1};
        RequestMessage req = new RequestMessage("op", Instant.now(), "C0");

        replica.recvPrePrepare(new PrePrepareMessage(wrongView, seq, digest, req));
        replica.recvPrepare(new PrepareMessage(wrongView, seq, digest, "B"));
        replica.recvCommit(new CommitMessage(wrongView, seq, digest, "C"));

        verify(messageLog, never()).newTicket(anyLong(), anyLong());
        verify(messageLog, never()).getTicket(anyLong(), anyLong());
        verify(replica, never()).broadcastMessage(any());
    }

    // 3) reject when not between watermarks
    @Test
    void rejectsPhaseMessages_outsideWatermarks() {
        doCallRealMethod().when(replica).recvPrePrepare(any());
        doCallRealMethod().when(replica).recvPrepare(any());
        doCallRealMethod().when(replica).recvCommit(any());

        long view = replica.getViewNumber();
        long seq = 9999L;
        byte[] digest = new byte[]{2};
        RequestMessage req = new RequestMessage("op", Instant.now(), "C0");

        // Force watermark check to fail
        when(messageLog.isBetweenWaterMarks(eq(seq))).thenReturn(false);

        replica.recvPrePrepare(new PrePrepareMessage(view, seq, digest, req));
        replica.recvPrepare(new PrepareMessage(view, seq, digest, "B"));
        replica.recvCommit(new CommitMessage(view, seq, digest, "C"));

        verify(messageLog, never()).newTicket(anyLong(), anyLong());
        verify(messageLog, never()).getTicket(anyLong(), anyLong());
        verify(replica, never()).broadcastMessage(any());
    }

    // 4) pre-prepare acceptance rules
    @Test
    void acceptsPrePrepare_whenNoPriorOrMatchingDigest_andBroadcastsPrepare_andAppendsBoth() {
        doCallRealMethod().when(replica).recvPrePrepare(any());

        long view = replica.getViewNumber();
        long seq = 5L;
        RequestMessage req = new RequestMessage("op", Instant.now(), "C1");

        // Stub digest computation to a known value matching the message
        byte[] digest = new byte[]{4, 5, 6};
        doReturn(digest).when(replica).digest(any());

        // Accepting scenario: sequence must be within watermarks
        when(messageLog.isBetweenWaterMarks(eq(seq))).thenReturn(true);

        // Case A: no existing ticket => create new
        when(messageLog.getTicket(eq(view), eq(seq))).thenReturn(null);
        @SuppressWarnings("unchecked")
        Ticket<Serializable, Serializable> newTicket = Mockito.mock(Ticket.class);
        when(messageLog.newTicket(eq(view), eq(seq))).thenReturn(newTicket);

        PrePrepareMessage ppFirst = new PrePrepareMessage(view, seq, digest, req);
        replica.recvPrePrepare(ppFirst);

        // Verify new ticket created and both appends
        verify(messageLog, times(1)).newTicket(eq(view), eq(seq));
        InOrder orderA = inOrder(newTicket);
        orderA.verify(newTicket).append(eq(ppFirst));
        orderA.verify(newTicket).append(argThat(a -> a instanceof PrepareMessage));
        verify(replica, times(1)).broadcastMessage(argThat(m -> m instanceof PrepareMessage));

        // Case B: existing ticket with same digest => accept
        @SuppressWarnings("unchecked")
        Ticket<Serializable, Serializable> existingTicket = Mockito.mock(Ticket.class);
        PrePrepareMessage existing = new PrePrepareMessage(view, seq, digest, req);
        when(existingTicket.getMessages()).thenReturn(java.util.List.of(existing));
        when(messageLog.getTicket(eq(view), eq(seq))).thenReturn(existingTicket);

        PrePrepareMessage ppSame = new PrePrepareMessage(view, seq, digest, req);
        replica.recvPrePrepare(ppSame);

        verify(existingTicket, times(1)).append(eq(ppSame));
        verify(existingTicket, times(1)).append(argThat(a -> a instanceof PrepareMessage));
        verify(replica, times(2)).broadcastMessage(argThat(m -> m instanceof PrepareMessage));
    }

    @Test
    void rejectsPrePrepare_whenExistingDigestDiffers() {
        doCallRealMethod().when(replica).recvPrePrepare(any());

        long view = replica.getViewNumber();
        long seq = 6L;
        RequestMessage req = new RequestMessage("op", Instant.now(), "C2");

        // Stub digest to match incoming PP digest
        byte[] digest = new byte[]{7, 7, 7};
        doReturn(digest).when(replica).digest(any());

        // Existing ticket contains a different PrePrepare digest
        @SuppressWarnings("unchecked")
        Ticket<Serializable, Serializable> existingTicket = Mockito.mock(Ticket.class);
        byte[] otherDigest = new byte[]{8, 8, 8};
        PrePrepareMessage prior = new PrePrepareMessage(view, seq, otherDigest, req);
        when(existingTicket.getMessages()).thenReturn(java.util.List.of(prior));
        when(messageLog.getTicket(eq(view), eq(seq))).thenReturn(existingTicket);

        PrePrepareMessage incoming = new PrePrepareMessage(view, seq, digest, req);
        replica.recvPrePrepare(incoming);

        // Should reject: no append and no broadcast
        verify(existingTicket, never()).append(any());
        verify(replica, never()).broadcastMessage(any());
        verify(messageLog, never()).newTicket(anyLong(), anyLong());
    }
}
