package byzzbench.simulator.protocols.faulty_safety;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple smoke test: instantiate the PbftJavaScenario, deliver events 1..4,
 * and assert that no correctness predicates are violated.
 */
@DisplayName("Faulty-Safety: Scenario smoke tests")
public class FaultySafetyScenarioSmokeTest {
    @Test
    @DisplayName("Scenario starts with all invariants satisfied")
    void scenarioIsCorrectAtStart() {
        // Build deterministic parameters to ensure reproducibility
        ScenarioParameters params = ScenarioParameters.builder()
                .scenarioId("faulty-safety")
                .randomSeed(0L)
                .numClients(1)
                .numReplicas(4)
                .build();

        // Create schedule and scenario
        Schedule schedule = new Schedule(params);
        Scenario scenario = new FaultySafetyScenario(schedule);

        // Final assert that none of the predicates are violated
        assertTrue(scenario.invariantsHold(),
                () -> "Unsatisfied invariants: " + scenario.unsatisfiedInvariants());
    }

    @Test
    @DisplayName("Delivering requests in different orders violates Agreement invariant")
    void deliveringClientRequestsInDifferentOrdersViolatesAgreement() {
        // Build deterministic parameters to ensure reproducibility
        ScenarioParameters params = ScenarioParameters.builder()
                .scenarioId("faulty-safety")
                .randomSeed(0L)
                .numClients(1)
                .numReplicas(4)
                .build();

        // Create schedule and scenario
        Schedule schedule = new Schedule(params);
        Scenario scenario = new FaultySafetyScenario(schedule);

        scenario.getTransport().deliverEvent(3);
        scenario.getTransport().deliverEvent(1);

        // Final assert that none of the predicates are violated
        assertFalse(scenario.invariantsHold(), () -> "Unsatisfied invariants: " + scenario.unsatisfiedInvariants());

        // one invariant was violated
        assertEquals(1, scenario.unsatisfiedInvariants().size());

        // it should be agreement
        assertEquals("Agreement", scenario.unsatisfiedInvariants().getFirst().getId());
    }

    @Test
    @DisplayName("Delivering requests in the same order preserves invariants")
    void deliveringClientRequestsInSameOrderWorks() {
        // Build deterministic parameters to ensure reproducibility
        ScenarioParameters params = ScenarioParameters.builder()
                .scenarioId("faulty-safety")
                .randomSeed(0L)
                .numClients(1)
                .numReplicas(4)
                .build();

        // Create schedule and scenario
        Schedule schedule = new Schedule(params);
        Scenario scenario = new FaultySafetyScenario(schedule);

        scenario.getTransport().deliverEvent(3);
        scenario.getTransport().deliverEvent(5);
        scenario.getTransport().deliverEvent(1);
        scenario.getTransport().deliverEvent(6);

        // Final assert that none of the predicates are violated
        assertTrue(scenario.invariantsHold(),
                () -> "Unsatisfied invariants: " + scenario.unsatisfiedInvariants());
    }
}
