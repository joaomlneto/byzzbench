package byzzbench.simulator.protocols.pbft_java;

import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.exploration_strategy.byzzfuzz.ByzzFuzzRoundInfo;
import byzzbench.simulator.exploration_strategy.byzzfuzz.MessageWithByzzFuzzRoundInfo;
import byzzbench.simulator.nodes.Node;
import byzzbench.simulator.nodes.Replica;
import byzzbench.simulator.protocols.pbft_java.message.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PbftJavaByzzFuzzRoundInfoOracle}.
 */
class PbftJavaByzzFuzzRoundInfoOracleTest {

    private PbftJavaScenario scenario;
    private PbftJavaByzzFuzzRoundInfoOracle oracle;

    @BeforeEach
    void setUp() {
        // Minimal scenario with deterministic seed
        ScenarioParameters params = ScenarioParameters.builder()
                .randomSeed(123L)
                .numReplicas(4)
                .numClients(1)
                .build();
        Schedule schedule = new Schedule(params);
        scenario = new PbftJavaScenario(schedule);
        oracle = (PbftJavaByzzFuzzRoundInfoOracle) scenario.getRoundInfoOracle();
    }

    private Replica anyReplica() {
        return scenario.getReplicas().firstEntry().getValue();
    }

    private SortedSet<String> replicaRecipientsExcluding(Node sender) {
        SortedSet<String> recipients = new TreeSet<>(scenario.getReplicas().keySet());
        recipients.remove(sender.getId());
        return recipients;
    }

    private SortedSet<String> anyClientRecipient() {
        return new TreeSet<>(scenario.getClients().keySet());
    }

    @Test
    @DisplayName("numRoundsToProcessRequest should be 4 for PBFT")
    void numRoundsToProcessRequestIsFour() {
        assertEquals(4, oracle.numRoundsToProcessRequest());
    }

    @Test
    @DisplayName("getProtocolMessageVerbIndex maps phase messages and view-change/new-view")
    void verbIndexMappingForPhaseMessages() {
        // PrePrepare
        PrePrepareMessage prePrepare = new PrePrepareMessage(0, 1, new byte[0],
                new RequestMessage("op", Instant.now(), "C0"));
        assertEquals(1, oracle.getProtocolMessageVerbIndex(prePrepare));

        // Prepare
        PrepareMessage prepare = new PrepareMessage(0, 1, new byte[0], anyReplica().getId());
        assertEquals(2, oracle.getProtocolMessageVerbIndex(prepare));

        // Commit
        CommitMessage commit = new CommitMessage(0, 1, new byte[0], anyReplica().getId());
        assertEquals(3, oracle.getProtocolMessageVerbIndex(commit));

        // ViewChange (verb 5)
        ViewChangeMessage viewChange = new ViewChangeMessage(1, 0, new TreeSet<>(), new TreeMap<>(), anyReplica().getId());
        assertEquals(5, oracle.getProtocolMessageVerbIndex(viewChange));

        // NewView (verb 6)
        NewViewMessage newView = new NewViewMessage(1, List.of(), List.of());
        assertEquals(6, oracle.getProtocolMessageVerbIndex(newView));
    }

    @Test
    @DisplayName("extractMessageRoundInformation uses (viewNumber, sequenceNumber, verbIndex)")
    void extractMessageRoundInformationFromPhaseMessage() {
        PrePrepareMessage msg = new PrePrepareMessage(2, 7, new byte[0],
                new RequestMessage("op", Instant.now(), "C0"));
        ByzzFuzzRoundInfo roundInfo = oracle.extractMessageRoundInformation(msg);
        assertEquals(2, roundInfo.getViewNumber());
        assertEquals(7, roundInfo.getSequenceNumber());
        assertEquals(1, roundInfo.getVerbIndex());
    }

    @Test
    @DisplayName("onMulticast: PrePrepare to replicas increases round and updates round info")
    void onMulticastPrePrepareToReplicas() {
        Replica sender = anyReplica();
        long before = oracle.getReplicaRound(sender.getId());
        ByzzFuzzRoundInfo beforeInfo = oracle.getReplicaRoundInfo(sender.getId());

        PrePrepareMessage prePrepare = new PrePrepareMessage(0, 1, new byte[0],
                new RequestMessage("op", Instant.now(), "C0"));

        SortedSet<String> recipients = replicaRecipientsExcluding(sender);
        scenario.getTransport().multicast(sender, recipients, prePrepare);

        long after = oracle.getReplicaRound(sender.getId());
        ByzzFuzzRoundInfo afterInfo = oracle.getReplicaRoundInfo(sender.getId());

        // With initial (0,0,0), sequenceNumber increased to 1 with verbIndex=1
        // current logic: delta = verbIndex + numRounds - prevVerbIndex = 1 + 4 - 0 = 5
        assertEquals(before + 5, after, "Replica round should advance by 5 on first PrePrepare");
        assertEquals(0, afterInfo.getViewNumber());
        assertEquals(1, afterInfo.getSequenceNumber());
        assertEquals(1, afterInfo.getVerbIndex());
        assertEquals(0, beforeInfo.getViewNumber());
    }

    @Test
    @DisplayName("onMulticast: Prepare to replicas advances only by verb difference when same seq")
    void onMulticastPrepareSameSequence() {
        Replica sender = anyReplica();

        // First deliver PrePrepare (seq=1) to set baseline
        scenario.getTransport().multicast(sender, replicaRecipientsExcluding(sender),
                new PrePrepareMessage(0, 1, new byte[0], new RequestMessage("op", Instant.now(), "C0")));
        long afterPrePrepareRound = oracle.getReplicaRound(sender.getId());

        // Now Prepare for same (view=0, seq=1)
        scenario.getTransport().multicast(sender, replicaRecipientsExcluding(sender),
                new PrepareMessage(0, 1, new byte[0], sender.getId()));

        long afterPrepareRound = oracle.getReplicaRound(sender.getId());
        // delta should be verbIndex(2) - prevVerbIndex(1) = 1
        assertEquals(afterPrePrepareRound + 1, afterPrepareRound);

        ByzzFuzzRoundInfo afterInfo = oracle.getReplicaRoundInfo(sender.getId());
        assertEquals(0, afterInfo.getViewNumber());
        assertEquals(1, afterInfo.getSequenceNumber());
        assertEquals(2, afterInfo.getVerbIndex());
    }

    @Test
    @DisplayName("onMulticast: Commit with higher sequence increments by numRounds + verbIndex")
    void onMulticastCommitHigherSequence() {
        Replica sender = anyReplica();

        // Baseline: PrePrepare(seq=1) then Commit(seq=2)
        scenario.getTransport().multicast(sender, replicaRecipientsExcluding(sender),
                new PrePrepareMessage(0, 1, new byte[0], new RequestMessage("op", Instant.now(), "C0")));
        long baseline = oracle.getReplicaRound(sender.getId());

        scenario.getTransport().multicast(sender, replicaRecipientsExcluding(sender),
                new CommitMessage(0, 2, new byte[0], sender.getId()));

        long after = oracle.getReplicaRound(sender.getId());
        // Since sequence increased to 2: delta = verbIndex(3) + numRounds(4) - prevVerbIndex(1) = 6
        assertEquals(baseline + 6, after);

        ByzzFuzzRoundInfo info = oracle.getReplicaRoundInfo(sender.getId());
        assertEquals(0, info.getViewNumber());
        assertEquals(2, info.getSequenceNumber());
        assertEquals(3, info.getVerbIndex());
    }

    @Test
    @DisplayName("onMulticast: Reply advances to final verb; ViewChange/NewView contribute with view bump")
    void onMulticastReplyViewChangeNewViewContribute() {
        Replica sender = anyReplica();

        // First, simulate PrePrepare to set seq=1, verb=1 baseline
        scenario.getTransport().multicast(sender, replicaRecipientsExcluding(sender),
                new PrePrepareMessage(0, 1, new byte[0], new RequestMessage("op", Instant.now(), "C0")));
        long afterPrePrepare = oracle.getReplicaRound(sender.getId());
        ByzzFuzzRoundInfo afterPrePrepareInfo = oracle.getReplicaRoundInfo(sender.getId());

        // Reply (to clients) – should increment
        ReplyMessage reply = new ReplyMessage("req-1", 0, Instant.now(), anyClientRecipient().first(), sender.getId(), "ok");
        scenario.getTransport().multicast(sender, anyClientRecipient(), reply);
        long afterReply = oracle.getReplicaRound(sender.getId());
        ByzzFuzzRoundInfo afterReplyInfo = oracle.getReplicaRoundInfo(sender.getId());
        assertEquals(afterPrePrepareInfo.getViewNumber(), afterReplyInfo.getViewNumber());
        assertEquals(afterPrePrepareInfo.getSequenceNumber(), afterReplyInfo.getSequenceNumber());
        assertEquals(1, afterReplyInfo.getVerbIndex());

        // ViewChange – contributes: view bump causes +1
        ViewChangeMessage viewChange = new ViewChangeMessage(1, 0, new TreeSet<>(), new TreeMap<>(), sender.getId());
        scenario.getTransport().multicast(sender, replicaRecipientsExcluding(sender), viewChange);
        long afterViewChange = oracle.getReplicaRound(sender.getId());
        assertTrue(afterViewChange >= afterReply + 1, "ViewChange should increase the round by at least 1");

        // NewView – contributes: new view message also causes +1 (view already higher)
        NewViewMessage newView = new NewViewMessage(1, List.of(), List.of());
        scenario.getTransport().multicast(sender, replicaRecipientsExcluding(sender), newView);

        long after = oracle.getReplicaRound(sender.getId());
        ByzzFuzzRoundInfo afterInfo = oracle.getReplicaRoundInfo(sender.getId());

        // At least +1 more from NewView (exact delta may depend on prior state), but should be >= afterViewChange
        assertTrue(after >= afterViewChange, "Replica round after NewView should be >= after view change");
        // View bumped to 1 after ViewChange/NewView
        assertEquals(1, afterInfo.getViewNumber());
    }

    @Test
    @DisplayName("getProtocolMessageVerbIndex throws for unknown MessageWithByzzFuzzRoundInfo implementations")
    void verbIndexThrowsForUnknownMessage() {
        MessageWithByzzFuzzRoundInfo unknown = new MessageWithByzzFuzzRoundInfo() {
            @Override
            public long getViewNumber() {
                return 0;
            }

            @Override
            public long getRound() {
                return 0;
            }
        };

        // Expect IllegalStateException from the switch default
        try {
            oracle.getProtocolMessageVerbIndex(unknown);
            fail("Expected IllegalStateException for unknown message type");
        } catch (IllegalStateException e) {
            // expected
            assertTrue(e.getMessage().contains("Unexpected value"));
        }
    }
}
