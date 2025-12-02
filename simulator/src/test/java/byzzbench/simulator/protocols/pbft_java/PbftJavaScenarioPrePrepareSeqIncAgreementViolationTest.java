package byzzbench.simulator.protocols.pbft_java;

import byzzbench.simulator.ScenarioPredicate;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.exploration_strategy.ExplorationStrategy;
import byzzbench.simulator.exploration_strategy.ExplorationStrategyParameters;
import byzzbench.simulator.exploration_strategy.fifo.FifoExplorationStrategy;
import byzzbench.simulator.nodes.LeaderBasedProtocolReplica;
import byzzbench.simulator.protocols.pbft_java.message.PrePrepareMessage;
import byzzbench.simulator.state.AgreementPredicate;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Optional;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Create a test for the PbftJavaScenario:
 * If one mutates the first preprepare message from the leader to one of the other replicas with the
 * 'pbft-preprepare-seq-inc' mutation… executing the protocol until the replicas commit two values each
 * should result in agreement violations.
 */
@DisplayName("PBFT-Java: PRE-PREPARE seq++ causes agreement violation after two commits")
public class PbftJavaScenarioPrePrepareSeqIncAgreementViolationTest {

    private static Optional<Long> findFirstPrePrepareFromLeader(PbftJavaScenario scenario, String leaderId) {
        return scenario.getTransport().getEvents().values().stream()
                .filter(e -> e.getStatus() == Event.Status.QUEUED)
                .filter(MessageEvent.class::isInstance)
                .map(MessageEvent.class::cast)
                .filter(me -> me.getSenderId().equals(leaderId))
                .filter(me -> !scenario.isFaultyReplica(me.getRecipientId())) // mutate to a non-faulty recipient
                .filter(me -> me.getPayload() instanceof PrePrepareMessage)
                .min(Comparator.comparingLong(Event::getEventId))
                .map(Event::getEventId);
    }

    private static Optional<Long> awaitFirstPrePrepareFromLeaderQueuedPreferA(PbftJavaScenario scenario, String leaderId, int maxSteps) {
        // Drive the scenario strictly via FIFO delivery decisions (no drops, no extra faults)
        FifoExplorationStrategy fifo = new FifoExplorationStrategy();
        for (int i = 0; i < maxSteps; i++) {
            // Prefer the PRE-PREPARE to replica A if A is not faulty (mirrors schedule that reveals the bug)
            Optional<Long> found = findFirstPrePrepareFromLeaderToRecipient(scenario, leaderId, "A");
            if (found.isEmpty()) {
                // Fallback: any non-faulty recipient
                found = findFirstPrePrepareFromLeader(scenario, leaderId);
            }
            if (found.isPresent()) {
                return found;
            }

            // No PRE-PREPARE yet: deliver next queued event to progress the protocol
            Optional<byzzbench.simulator.domain.Action> decision = fifo.scheduleNext(scenario);
            if (decision.isEmpty()) {
                break; // nothing else to deliver
            }
        }
        return Optional.empty();
    }

    private static Optional<Long> findFirstPrePrepareFromLeaderToRecipient(PbftJavaScenario scenario, String leaderId, String recipientId) {
        if (scenario.isFaultyReplica(recipientId)) {
            return Optional.empty();
        }
        return scenario.getTransport().getEvents().values().stream()
                .filter(e -> e.getStatus() == Event.Status.QUEUED)
                .filter(MessageEvent.class::isInstance)
                .map(MessageEvent.class::cast)
                .filter(me -> me.getSenderId().equals(leaderId))
                .filter(me -> me.getRecipientId().equals(recipientId))
                .filter(me -> me.getPayload() instanceof PrePrepareMessage)
                .min(Comparator.comparingLong(Event::getEventId))
                .map(Event::getEventId);
    }

    private static void pumpUntilTwoCommitsPerReplicaOrExhaustion(PbftJavaScenario scenario, int maxSteps) {
        FifoExplorationStrategy strategy = new FifoExplorationStrategy();
        // Ensure deterministic defaults (no drops, no extra mutations)
        strategy.loadParameters(new ExplorationStrategyParameters());

        int steps = 0;
        while (steps < maxSteps && minCommittedEntriesAcrossNonFaultyReplicas(scenario) < 2) {
            // Ask FIFO strategy what to do next; it delivers the first queued message if any
            Optional<byzzbench.simulator.domain.Action> decision = strategy.scheduleNext(scenario);

            // If there is nothing to deliver, we break to avoid injecting faults or dropping messages
            if (decision.isEmpty()) {
                break;
            }

            steps++;
        }
    }

    private static int minCommittedEntriesAcrossNonFaultyReplicas(PbftJavaScenario scenario) {
        return scenario.getReplicas().values().stream()
                .filter(r -> !scenario.isFaultyReplica(r.getId()))
                .mapToInt(r -> r.getCommitLog().getLength())
                .min()
                .orElse(0);
    }

    @Test
    @DisplayName("Mutating first PRE-PREPARE (seq++) leads to Agreement violation after two commits")
    void prePrepareSeqIncCausesAgreementViolationAtTwoCommits() {
        // Deterministic scenario
        ScenarioParameters params = ScenarioParameters.builder()
                .scenarioId("pbft-java-preprepare-seq-inc")
                .randomSeed(42L)
                .numClients(1)
                .numReplicas(4)
                .build();

        Schedule schedule = new Schedule(params);
        PbftJavaScenario scenario = new PbftJavaScenario(schedule);
        ExplorationStrategy strategy = new FifoExplorationStrategy();
        strategy.loadParameters(new ExplorationStrategyParameters());

        // Identify leader for view 1 (PBFT-Java replicas set view to 1 at init)
        LeaderBasedProtocolReplica anyReplica = (LeaderBasedProtocolReplica) scenario.getReplicas().firstEntry().getValue();
        String leaderId = anyReplica.getRoundRobinPrimaryId(1);

        // Advance the simulation until the first queued PRE-PREPARE from leader is present, then capture it
        long prePrepareEventId = awaitFirstPrePrepareFromLeaderQueuedPreferA(scenario, leaderId, 30_000)
                .orElseThrow(() -> new AssertionError("Could not find the first PRE-PREPARE from leader " + leaderId + " within step limit"));

        // Directly mutate the message payload: increment sequence number and re-sign
        Event e = scenario.getTransport().getEvent(prePrepareEventId);
        assertNotNull(e, "Event not found by id: " + prePrepareEventId);
        assertInstanceOf(MessageEvent.class, e, "Event is not a MessageEvent");
        MessageEvent me = (MessageEvent) e;
        assertInstanceOf(PrePrepareMessage.class, me.getPayload(), "Message payload is not PrePrepareMessage");
        PrePrepareMessage original = (PrePrepareMessage) me.getPayload();
        PrePrepareMessage mutated = original.withSequenceNumber(original.getSequenceNumber() + 1);
        mutated.sign(original.getSignedBy());
        me.setPayload(mutated);

        // Now drive the protocol until every non-faulty replica has at least two commits
        pumpUntilTwoCommitsPerReplicaOrExhaustion(scenario, 300);

        int minCommitted = minCommittedEntriesAcrossNonFaultyReplicas(scenario);
        assertTrue(minCommitted >= 2, () -> "Expected at least two commits per non-faulty replica, but minCommitted=" + minCommitted);

        // print the schedule
        scenario.getSchedule().getActions().forEach(System.out::println);

        // At this point, Agreement must be violated
        boolean holds = scenario.invariantsHold();
        if (holds) {
            // Provide debugging help if the expectation fails
            SortedMap<Long, Event> events = scenario.getTransport().getEvents();
            long queued = events.values().stream().filter(ev -> ev.getStatus() == Event.Status.QUEUED).count();
            long delivered = events.values().stream().filter(ev -> ev.getStatus() == Event.Status.DELIVERED).count();
            fail("Expected Agreement violation after two commits each, but invariants hold. Queued=" + queued + ", delivered=" + delivered);
        }

        // Specifically check Agreement is among the unsatisfied invariants
        boolean agreementViolated = scenario.unsatisfiedInvariants().stream()
                .anyMatch(p -> (p instanceof AgreementPredicate) || "Agreement".equals(p.getId()));

        assertTrue(agreementViolated, () -> "Expected Agreement predicate to be violated, but got: "
                + scenario.unsatisfiedInvariants().stream().map(ScenarioPredicate::getId).toList());
    }
}
