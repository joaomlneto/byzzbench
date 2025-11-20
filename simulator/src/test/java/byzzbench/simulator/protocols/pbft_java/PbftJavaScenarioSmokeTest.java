package byzzbench.simulator.protocols.pbft_java;

import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simple smoke test: instantiate the PbftJavaScenario, deliver events 1..4,
 * and assert that no correctness predicates are violated.
 */
public class PbftJavaScenarioSmokeTest {
    @Test
    void deliveringFirstThreeMessagesDoNotViolateInvariants() {
        // Build deterministic parameters to ensure reproducibility
        ScenarioParameters params = ScenarioParameters.builder()
                .scenarioId("pbft-java")
                .randomSeed(0L)
                .numClients(1)
                .numReplicas(4)
                .build();

        // Create schedule and scenario
        Schedule schedule = new Schedule(params);
        PbftJavaScenario scenario = new PbftJavaScenario(schedule);

        // Deliver the first three events (ids start at 1 in Transport)
        for (long id = 1; id <= 3; id++) {
            long eid = id; // capture for lambda below
            scenario.getTransport().deliverEvent(eid, true);
            // Optionally, check after each delivery to catch early violations
            assertTrue(scenario.invariantsHold(),
                    () -> "Invariants violated after delivering event id=" + eid
                            + ": " + scenario.unsatisfiedInvariants());
        }

        // Final assert that none of the predicates are violated
        assertTrue(scenario.invariantsHold(),
                () -> "Unsatisfied invariants: " + scenario.unsatisfiedInvariants());
    }
}
