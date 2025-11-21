package byzzbench.simulator.exploration_strategy.byzzfuzz;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.domain.Campaign;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.domain.Action;
import byzzbench.simulator.domain.DeliverMessageAction;
import byzzbench.simulator.domain.FaultInjectionAction;
import byzzbench.simulator.domain.DropMessageAction;
import byzzbench.simulator.exploration_strategy.ExplorationStrategyParameters;
import byzzbench.simulator.exploration_strategy.random.RandomExplorationStrategy;
import byzzbench.simulator.protocols.pbft_java.PbftJavaScenario;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests for ByzzFuzzExplorationStrategy covering:
 * - Network faults and process faults applicability
 * - Round number inference via the ByzzFuzz round oracle
 * - Exclusivity: when a fault is enabled, only that fault action is exposed
 */
@DisplayName("ByzzFuzz exploration strategy")
public class ByzzFuzzExplorationStrategyTest {

    private Schedule buildScheduleWithCampaign(long seed, Map<String, String> params) {
        ScenarioParameters sp = ScenarioParameters.builder()
                .scenarioId("byzzfuzz-test")
                .randomSeed(seed)
                .numClients(1)
                .numReplicas(4)
                .build();
        Schedule schedule = new Schedule(sp);

        ExplorationStrategyParameters esp = new ExplorationStrategyParameters();
        esp.setExplorationStrategyId(ByzzFuzzExplorationStrategy.class.getName());
        esp.setRandomSeed(seed);
        esp.setParams(new HashMap<>(params));

        Campaign c = new Campaign();
        c.setInitialRandomSeed(seed);
        c.setExplorationStrategyParameters(esp);
        schedule.setCampaign(c);
        return schedule;
    }

    /**
     * Installs a minimal mocked ApplicationContext so that MutateMessageBehavior can obtain
     * a MessageMutatorService with at least one mutator applicable to PBFT messages.
     */
    @SuppressWarnings("unchecked")
    private void installTestApplicationContext() {
        ApplicationContext mockCtx = Mockito.mock(ApplicationContext.class);

        // Create a dummy mutator that claims to handle common PBFT message payload classes
        class DummyMutator extends byzzbench.simulator.faults.faults.MessageMutationFault {
            public DummyMutator(String id, String name, Collection<Class<? extends java.io.Serializable>> inputClasses) {
                super(id, name, inputClasses);
            }
        }

        // PBFT payload classes we expect in the first round
        java.util.List<Class<? extends java.io.Serializable>> classes = new ArrayList<>();
        classes.add(byzzbench.simulator.protocols.pbft_java.message.PrePrepareMessage.class);
        classes.add(byzzbench.simulator.protocols.pbft_java.message.PrepareMessage.class);
        classes.add(byzzbench.simulator.protocols.pbft_java.message.CommitMessage.class);

        DummyMutator dummy = new DummyMutator("dummy-mutator", "Dummy", classes);
        byzzbench.simulator.service.MessageMutatorService mms =
                new byzzbench.simulator.service.MessageMutatorService(List.of(dummy), List.of());

        when(mockCtx.getBean(byzzbench.simulator.service.MessageMutatorService.class)).thenReturn(mms);

        // Install the mock application context
        new byzzbench.simulator.service.ApplicationContextProvider().setApplicationContext(mockCtx);
    }

    /**
     * Deliver the first client request so that inter-replica messages are queued.
     */
    private void deliverFirstClientRequest(Scenario scenario) {
        // Transport event ids start at 1; the very first message is the client request
        scenario.getTransport().deliverEvent(1L, true);
    }

    /**
     * Helper to find the first queued inter-replica message (MessageEvent) id.
     */
    private Optional<MessageEvent> findFirstQueuedReplicaMessage(Scenario scenario) {
        return scenario.getTransport().getEventsInState(Event.Status.QUEUED).stream()
                .filter(MessageEvent.class::isInstance)
                .map(MessageEvent.class::cast)
                .filter(me -> scenario.getReplicas().containsKey(me.getSenderId()))
                .findFirst();
    }

    @Test
    @DisplayName("Network faults are applied exclusively and round is inferred")
    void networkFaultsAreAppliedAndExclusive_andRoundIsInferred() {
        // Configure ByzzFuzz to have only network faults targeting a single round (round=1)
        Map<String, String> params = Map.of(
                "numRoundsWithProcessFaults", "0",
                "numRoundsWithNetworkFaults", "1",
                "numRoundsWithFaults", "1"
        );

        Schedule schedule = buildScheduleWithCampaign(123L, params);
        PbftJavaScenario scenario = new PbftJavaScenario(schedule);

        // Strategy with deterministic seed
        ByzzFuzzExplorationStrategy strategy = new ByzzFuzzExplorationStrategy();
        strategy.loadParameters(schedule.getCampaign().getExplorationStrategyParameters());

        // First, deliver the initial client request to produce inter-replica messages
        deliverFirstClientRequest(scenario);

        // Now ByzzFuzz should see at least one inter-replica message with an inferred round
        // Since numRoundsWithFaults=1, the generated fault predicates on round==1
        List<Action> actions = strategy.getAvailableActions(scenario);

        // Exclusivity: if a fault is enabled, the strategy must expose only that fault action
        assertEquals(1, actions.size(), "ByzzFuzz should expose exactly one action (the applicable fault)");
        Action only = actions.get(0);
        assertTrue(only instanceof DropMessageAction,
                "Expected a DropMessageAction produced by ByzzFuzzNetworkFault when enabled");

        // Validate the action targets a queued message and round inference exists
        long targetEventId = ((DropMessageAction) only).getEventId();
        Event targeted = scenario.getTransport().getEvent(targetEventId);
        assertNotNull(targeted, "Target event must exist");
        assertEquals(Event.Status.QUEUED, targeted.getStatus(), "Target event should be queued before executing fault");

        // Round oracle must have an entry for this message (round==1)
        long inferredRound = scenario.getRoundInfoOracle().getMessageRounds().getOrDefault(targetEventId, 0L);
        assertEquals(1L, inferredRound, "Round oracle should infer round=1 for targeted message");

        // Execute the action and verify the message is dropped
        only.accept(scenario);
        assertEquals(Event.Status.DROPPED, scenario.getTransport().getEvent(targetEventId).getStatus(),
                "Message should be dropped by the network fault");
    }

    @Test
    @DisplayName("Process faults use round oracle and are exclusive when enabled")
    void processFaultsAreAppliedBasedOnRoundOracle_andExclusive() {
        // Configure ByzzFuzz to have only process faults at round 1
        Map<String, String> params = Map.of(
                "numRoundsWithProcessFaults", "1",
                "numRoundsWithNetworkFaults", "0",
                "numRoundsWithFaults", "1"
        );

        Schedule schedule = buildScheduleWithCampaign(321L, params);
        PbftJavaScenario scenario = new PbftJavaScenario(schedule);

        // Direct the client request to the presumed initial leader (A) so that
        // standard PBFT inter-replica messages (pre-prepare/prepare/commit) are produced.
        String clientId = scenario.getClients().firstKey();
        scenario.getClients().get(clientId).sendRequest("req-leader-1", "A");
        // Deliver the client->leader request to kick off the protocol round
        Optional<MessageEvent> toLeader = scenario.getTransport().getEventsInState(Event.Status.QUEUED).stream()
                .filter(MessageEvent.class::isInstance)
                .map(MessageEvent.class::cast)
                .filter(me -> me.getRecipientId().equals("A"))
                .findFirst();
        assertTrue(toLeader.isPresent(), "Directed client request to leader A should be queued");
        scenario.getTransport().deliverEvent(toLeader.get().getEventId(), true);

        // Strategy with deterministic seed
        ByzzFuzzExplorationStrategy strategy = new ByzzFuzzExplorationStrategy();
        strategy.loadParameters(schedule.getCampaign().getExplorationStrategyParameters());

        // Advance the system, if necessary, until a process fault becomes applicable.
        // Prepare mocked ApplicationContext for MutateMessageBehavior -> MessageMutatorService.
        installTestApplicationContext();

        // We do this deterministically by repeatedly delivering the earliest queued
        // replica message until ByzzFuzz reports a single FaultInjectionAction.
        FaultInjectionAction fia = null;
        for (int i = 0; i < 20; i++) {
            List<Action> actions = strategy.getAvailableActions(scenario);
            if (actions.size() == 1 && actions.get(0) instanceof FaultInjectionAction) {
                fia = (FaultInjectionAction) actions.get(0);
                break;
            }
            // Otherwise, deliver the next inter-replica message to progress the round
            Optional<MessageEvent> nextReplicaMsg = scenario.getTransport().getEventsInState(Event.Status.QUEUED).stream()
                    .filter(MessageEvent.class::isInstance)
                    .map(MessageEvent.class::cast)
                    .filter(me -> scenario.getReplicas().containsKey(me.getSenderId()))
                    .min(Comparator.comparingLong(Event::getEventId));
            assertTrue(nextReplicaMsg.isPresent(), "Expected a queued replica message to progress the protocol");
            scenario.getTransport().deliverEvent(nextReplicaMsg.get().getEventId(), true);
        }

        assertNotNull(fia, "ByzzFuzz should eventually expose exactly one process-fault action");

        // Validate the target message id has round==1 according to the oracle
        long messageId = fia.getMessageId();
        long inferredRound = scenario.getRoundInfoOracle().getMessageRounds().getOrDefault(messageId, 0L);
        assertEquals(1L, inferredRound, "Round oracle should infer round=1 for targeted message");

        // Do NOT call accept on FaultInjectionAction here to avoid Spring MessageMutatorService dependency.
        // The purpose of this test is to verify ByzzFuzz decision-making and round inference, and that
        // no other actions are presented when a process fault is enabled (exclusivity).
    }
}
