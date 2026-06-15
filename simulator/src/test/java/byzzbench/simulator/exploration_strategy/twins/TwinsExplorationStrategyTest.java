package byzzbench.simulator.exploration_strategy.twins;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.domain.Campaign;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.exploration_strategy.ExplorationStrategyParameters;
import byzzbench.simulator.exploration_strategy.byzzfuzz.MessageWithByzzFuzzRoundInfo;
import byzzbench.simulator.nodes.Replica;
import byzzbench.simulator.protocols.pbft_java.PbftJavaScenario;
import byzzbench.simulator.transport.MessagePayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the Twins exploration strategy installs a <b>single, global,
 * per-round network partition</b> shared by every replica (rather than an
 * independent, randomly-generated partition per twin), and that this partition
 * is enforced on honest-to-honest links — not only on the twin's own links.
 */
@DisplayName("Twins: global per-round partitioning")
class TwinsExplorationStrategyTest {

    private static final int NUM_ROUNDS = 5;
    private static final int NUM_TWINS = 2;

    private Scenario scenario;
    private TwinsTransport transport;
    private String victimId;
    private List<String> honestIds;

    @BeforeEach
    void setup() {
        Schedule schedule = new Schedule(ScenarioParameters.builder()
                .scenarioId("pbft-java")
                .randomSeed(0L)
                .numClients(1)
                .numReplicas(4)
                .build());

        ExplorationStrategyParameters strategyParams = new ExplorationStrategyParameters();
        strategyParams.setRandomSeed(0L);
        strategyParams.getParams().put("numReplicas", "1");
        strategyParams.getParams().put("numTwinsPerReplica", String.valueOf(NUM_TWINS));
        strategyParams.getParams().put("numRounds", String.valueOf(NUM_ROUNDS));

        Campaign campaign = new Campaign();
        campaign.setExplorationStrategyParameters(strategyParams);
        schedule.setCampaign(campaign);

        scenario = new PbftJavaScenario(schedule);

        // Ensure there is a compromised replica for the strategy to twin.
        scenario.markReplicaFaulty(scenario.getReplicas().firstKey());

        TwinsExplorationStrategy strategy = new TwinsExplorationStrategy();
        strategy.initializeScenario(scenario);

        // Discover which replica was turned into a twin.
        victimId = scenario.getReplicas().values().stream()
                .filter(TwinsReplica.class::isInstance)
                .map(Replica::getId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no TwinsReplica was created"));
        honestIds = scenario.getReplicas().keySet().stream()
                .filter(id -> !id.equals(victimId))
                .toList();

        transport = (TwinsTransport) scenario.getReplicas().get(honestIds.get(0)).getTransport();
    }

    @Test
    @DisplayName("the compromised replica is replaced by a multiplexing TwinsReplica")
    void victimBecomesTwinsReplica() {
        assertTrue(scenario.getNodes().get(victimId) instanceof TwinsReplica);
    }

    @Test
    @DisplayName("all replicas (honest and twin instances) share ONE global partition schedule")
    void allReplicasShareOneGlobalTransport() {
        // every honest replica uses the same shared transport object
        for (String id : honestIds) {
            assertSame(transport, scenario.getReplicas().get(id).getTransport(),
                    "honest replica " + id + " should use the shared global Twins transport");
        }

        // the twin wrapper and all of its internal instances use the same transport
        TwinsReplica twin = (TwinsReplica) scenario.getNodes().get(victimId);
        assertSame(transport, twin.getTransport());
        for (Replica instance : twin.getReplicas()) {
            assertSame(transport, instance.getTransport(),
                    "twin instance should use the shared global Twins transport");
        }

        // exactly one schedule, covering numRounds rounds
        assertEquals(NUM_ROUNDS, transport.getRoundPartitions().size());
    }

    @Test
    @DisplayName("honest-to-honest delivery obeys the global partition (and is cut across partitions)")
    void honestToHonestRespectsPartitions() {
        int blocked = 0;
        for (long round = 0; round < NUM_ROUNDS; round++) {
            MessagePayload msg = new RoundMessage(round);
            for (String from : honestIds) {
                Replica sender = scenario.getReplicas().get(from);
                List<String> senderPartition = transport.getPartitionContaining(from, round);
                assertNotNull(senderPartition, from + " must belong to some partition");
                for (String to : honestIds) {
                    if (from.equals(to)) {
                        continue;
                    }
                    boolean samePartition = senderPartition.contains(to);
                    boolean canSend = transport.canSendMessage(sender, to, msg);
                    assertEquals(samePartition, canSend,
                            "round " + round + ": " + from + " -> " + to
                                    + " delivery must match partition membership");
                    if (!canSend) {
                        blocked++;
                    }
                }
            }
        }
        // The whole point of the fix: the global partition actually cuts some
        // honest-to-honest links (previously they were never partitioned).
        assertTrue(blocked > 0,
                "expected the global partition to block some honest-to-honest messages");
    }

    @Test
    @DisplayName("a twin can be split across partitions, so only the co-located instance handles a message")
    void twinInstancesCanBeSplit() {
        TwinsReplica twin = (TwinsReplica) scenario.getNodes().get(victimId);
        List<String> internalIds = twin.getInternalIds();

        // find a round where the twin's two instances are in different partitions
        long splitRound = -1;
        for (long round = 0; round < NUM_ROUNDS; round++) {
            List<String> p0 = transport.getPartitionContaining(internalIds.get(0), round);
            if (p0 != null && !p0.contains(internalIds.get(1))) {
                splitRound = round;
                break;
            }
        }
        assertTrue(splitRound >= 0, "expected at least one round where the twin is split across partitions");

        // a round message from an honest node must be handled only by the twin
        // instance(s) that share that honest node's partition
        String honest = honestIds.get(0);
        List<String> honestPartition = transport.getPartitionContaining(honest, splitRound);
        assertNotNull(honestPartition);
        long expected = internalIds.stream().filter(honestPartition::contains).count();

        List<Replica> handlers = twin.getInternalReplicasHandlingMessage(honest, new RoundMessage(splitRound));
        assertEquals(expected, handlers.size(),
                "only twin instances in the sender's partition should handle the message");
    }

    /** A minimal message that carries a round number (as ByzzFuzz/Twins expect). */
    private static class RoundMessage extends MessagePayload implements MessageWithByzzFuzzRoundInfo {
        private final long round;

        RoundMessage(long round) {
            this.round = round;
        }

        @Override
        public String getType() {
            return "TEST_ROUND_MESSAGE";
        }

        @Override
        public long getViewNumber() {
            return round;
        }

        @Override
        public long getRound() {
            return round;
        }
    }
}
