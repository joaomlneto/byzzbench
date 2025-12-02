package byzzbench.simulator.exploration_strategy.byzzfuzz;

import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.nodes.Node;
import byzzbench.simulator.nodes.Replica;
import byzzbench.simulator.protocols.pbft_java.PbftJavaScenario;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.MessagePayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Tests for the core logic implemented in {@link ByzzFuzzRoundInfoOracle} using a small test oracle
 * and a dummy message that implements {@link MessageWithByzzFuzzRoundInfo}.
 */
class ByzzFuzzRoundInfoOracleTest {

    private PbftJavaScenario scenario;
    private TestOracle oracle;

    @BeforeEach
    void setUp() {
        ScenarioParameters params = ScenarioParameters.builder()
                .randomSeed(123L)
                .numReplicas(4)
                .numClients(1)
                .build();
        Schedule schedule = new Schedule(params);
        scenario = new PbftJavaScenario(schedule);
        // Remove any existing transport observers so tests use the TestOracle exclusively
        oracle = new TestOracle(scenario);
        // ensure the transport observers contain only our TestOracle (remove any other ByzzFuzzRoundInfoOracle)
        scenario.getTransport().getObservers().removeIf(o -> o != oracle);
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
    @DisplayName("onMulticast: payload not MessageWithByzzFuzzRoundInfo is ignored")
    void multicastNonByzzFuzzMessageIgnored() {
        Replica sender = anyReplica();
        long before = oracle.getReplicaRound(sender.getId());

        // plain MessagePayload (anonymous) that does NOT implement MessageWithByzzFuzzRoundInfo
        MessagePayload p = new MessagePayload() {
            @Override
            public String getType() {
                return "plain";
            }
        };

        // multicast to replicas - should be ignored by oracle
        scenario.getTransport().multicast(sender, replicaRecipientsExcluding(sender), p);

        long after = oracle.getReplicaRound(sender.getId());
        assertEquals(before, after, "Replica round should not change when payload lacks ByzzFuzz info");
    }

    @Test
    @DisplayName("onMulticast: verb index zero to replicas is skipped")
    void multicastVerbZeroSkipped() {
        Replica sender = anyReplica();
        long before = oracle.getReplicaRound(sender.getId());

        DummyMessage msg = new DummyMessage(0, 1, 0); // verbIndex == 0
        scenario.getTransport().multicast(sender, replicaRecipientsExcluding(sender), msg);

        long after = oracle.getReplicaRound(sender.getId());
        assertEquals(before, after, "Replica round must not change when verbIndex is zero");
    }

    @Test
    @DisplayName("onMulticast: sequence increase advances rounds by verb+numRounds-prevVerb")
    void multicastSequenceIncreaseAdvances() {
        Replica sender = anyReplica();
        long before = oracle.getReplicaRound(sender.getId());

        // initial message: seq=1, verb=1
        DummyMessage m1 = new DummyMessage(0, 1, 1);
        scenario.getTransport().multicast(sender, replicaRecipientsExcluding(sender), m1);
        long afterFirst = oracle.getReplicaRound(sender.getId());
        // delta = verb(1) + numRounds(3) - prevVerb(0) = 4
        assertEquals(before + 4, afterFirst);

        // now send message with higher sequence 2 and verb 2
        DummyMessage m2 = new DummyMessage(0, 2, 2);
        scenario.getTransport().multicast(sender, replicaRecipientsExcluding(sender), m2);
        long afterSecond = oracle.getReplicaRound(sender.getId());
        // delta = verb(2) + numRounds(3) - prevVerb(1) = 4
        assertEquals(afterFirst + 4, afterSecond);
    }

    @Test
    @DisplayName("onMulticast: higher verb on same seq advances by verb difference")
    void multicastHigherVerbSameSeq() {
        Replica sender = anyReplica();

        DummyMessage m1 = new DummyMessage(0, 5, 1);
        scenario.getTransport().multicast(sender, replicaRecipientsExcluding(sender), m1);
        long afterFirst = oracle.getReplicaRound(sender.getId());

        DummyMessage m2 = new DummyMessage(0, 5, 3); // same seq, higher verb
        scenario.getTransport().multicast(sender, replicaRecipientsExcluding(sender), m2);
        long afterSecond = oracle.getReplicaRound(sender.getId());

        // delta should be verb difference: 3 - 1 = 2
        assertEquals(afterFirst + 2, afterSecond);
    }

    @Test
    @DisplayName("onMulticast to clients increments by 1 and sets final verb")
    void multicastToClientsIncrementsAndSetsFinalVerb() {
        Replica sender = anyReplica();

        // ensure some baseline round info
        DummyMessage prep = new DummyMessage(0, 7, 1);
        scenario.getTransport().multicast(sender, replicaRecipientsExcluding(sender), prep);
        long afterPrep = oracle.getReplicaRound(sender.getId());

        // now multicast a reply to client(s)
        DummyMessage reply = new DummyMessage(0, 7, 3);
        scenario.getTransport().multicast(sender, anyClientRecipient(), reply);

        long afterReply = oracle.getReplicaRound(sender.getId());
        // onMulticastToClients increments by +1
        assertEquals(afterPrep + 1, afterReply);

        // replica round info verb should be set to final verb (numRoundsToProcessRequest == 3)
        assertEquals(3, oracle.getReplicaRoundInfo(sender.getId()).getVerbIndex());
    }

    @Test
    @DisplayName("onMulticast: recipients both client and replica throws")
    void multicastToBothClientAndReplicaThrows() {
        Replica sender = anyReplica();

        SortedSet<String> recipients = new TreeSet<>();
        recipients.addAll(replicaRecipientsExcluding(sender));
        recipients.addAll(anyClientRecipient());

        DummyMessage m = new DummyMessage(0, 1, 1);
        assertThrows(IllegalStateException.class, () -> scenario.getTransport().multicast(sender, recipients, m));
    }

    @Test
    @DisplayName("onMulticast: recipients neither client nor replica throws")
    void multicastToNeitherClientNorReplicaThrows() {
        Replica sender = anyReplica();
        SortedSet<String> recipients = new TreeSet<>(); // empty
        DummyMessage m = new DummyMessage(0, 1, 1);
        assertThrows(IllegalStateException.class, () -> scenario.getTransport().multicast(sender, recipients, m));
    }

    // Note: tests for onEventAdded/onEventDelivered observer callbacks are intentionally omitted here
    // because exercising those callbacks requires tightly-coupled transport and replica behavior. The
    // core round-computation logic is exercised via the onMulticast tests above which cover the same
    // decision branches (view bump, sequence increase, verb index differences) in a deterministic way.

    @Test
    @DisplayName("onEventDelivered ignores messages with verbIndex==0")
    void onEventDeliveredIgnoresVerbZero() {
        Replica sender = anyReplica();
        DummyMessage m = new DummyMessage(0, 31, 0);

        String recipientId = scenario.getReplicas().lastKey();

        MessageEvent me = mock(MessageEvent.class, withSettings().extraInterfaces(MessageWithByzzFuzzRoundInfo.class));
        when(me.getEventId()).thenReturn(34567L);
        when(me.getSenderId()).thenReturn(sender.getId());
        when(me.getRecipientId()).thenReturn(recipientId);
        when(me.getPayload()).thenReturn(m);
        when(((MessageWithByzzFuzzRoundInfo) me).getViewNumber()).thenReturn(m.getViewNumber());
        when(((MessageWithByzzFuzzRoundInfo) me).getRound()).thenReturn(m.getRound());

        long before = oracle.getReplicaRound(recipientId);
        oracle.onEventDelivered(me);
        long after = oracle.getReplicaRound(recipientId);
        assertEquals(before, after, "Delivering a verbIndex==0 message should not change replica's round");
    }

    // --- Additional edge-case tests ---

    @Test
    @DisplayName("onMulticast: message with higher view causes view bump and +1 round")
    void multicastViewBumpUpdatesViewAndRound() {
        Replica sender = anyReplica();
        long before = oracle.getReplicaRound(sender.getId());
        // message with higher view number
        DummyMessage m = new DummyMessage(2, 0, 1);
        scenario.getTransport().multicast(sender, replicaRecipientsExcluding(sender), m);
        long after = oracle.getReplicaRound(sender.getId());
        assertEquals(before + 1, after, "Higher view should increment replica round by 1");
        assertEquals(2, oracle.getReplicaRoundInfo(sender.getId()).getViewNumber(), "Replica view should be updated to message view");
    }

    @Test
    @DisplayName("onMulticast: messages from non-replica senders are ignored by oracle")
    void multicastFromNonReplicaIgnored() {
        // pick a client as sender
        var client = scenario.getClients().firstEntry().getValue();
        Replica observed = anyReplica();
        long before = oracle.getReplicaRound(observed.getId());

        DummyMessage m = new DummyMessage(0, 1, 1);
        scenario.getTransport().multicast(client, replicaRecipientsExcluding(client), m);

        long after = oracle.getReplicaRound(observed.getId());
        assertEquals(before, after, "Oracle should ignore multicasts originating from non-replica nodes");
    }

    @Test
    @DisplayName("onMulticast: if extractMessageRoundInformation returns null, nothing changes")
    void multicastWhenExtractReturnsNullDoesNothing() {
        // create an oracle that returns null from extractMessageRoundInformation
        TestOracle nullOracle = new TestOracle(scenario) {
            @Override
            public ByzzFuzzRoundInfo extractMessageRoundInformation(MessageWithByzzFuzzRoundInfo messageWithRoundInfo) {
                return null;
            }
        };

        Replica sender = anyReplica();
        long before = nullOracle.getReplicaRound(sender.getId());
        DummyMessage m = new DummyMessage(0, 1, 1);
        // should not throw and should not change replica round
        scenario.getTransport().multicast(sender, replicaRecipientsExcluding(sender), m);
        long after = nullOracle.getReplicaRound(sender.getId());
        assertEquals(before, after);
    }

    // --- Helper test classes ---

    private static class DummyMessage extends MessagePayload implements MessageWithByzzFuzzRoundInfo {
        private final long viewNumber;
        private final long round;
        private final int verbIndex;

        public DummyMessage(long viewNumber, long round, int verbIndex) {
            this.viewNumber = viewNumber;
            this.round = round;
            this.verbIndex = verbIndex;
        }

        @Override
        public long getViewNumber() {
            return viewNumber;
        }

        @Override
        public long getRound() {
            return round;
        }

        public int getVerbIndex() {
            return verbIndex;
        }

        @Override
        public String getType() {
            return "Dummy";
        }
    }

    private static class TestOracle extends ByzzFuzzRoundInfoOracle {
        public TestOracle(byzzbench.simulator.Scenario scenario) {
            super(scenario);
        }

        @Override
        public int getProtocolMessageVerbIndex(MessageWithByzzFuzzRoundInfo message) {
            // handle payload wrapped in a MessageEvent mock
            if (message instanceof MessageEvent me && me.getPayload() instanceof DummyMessage dm) {
                return dm.getVerbIndex();
            }
            if (message instanceof DummyMessage dm) {
                return dm.getVerbIndex();
            }
            return 0;
        }

        @Override
        public int numRoundsToProcessRequest() {
            return 3;
        }
    }
}
