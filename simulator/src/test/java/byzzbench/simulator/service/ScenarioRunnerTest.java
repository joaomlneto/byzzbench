package byzzbench.simulator.service;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.config.TerminationConfig;
import byzzbench.simulator.domain.Action;
import byzzbench.simulator.domain.Campaign;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.exploration_strategy.ExplorationStrategy;
import byzzbench.simulator.exploration_strategy.ExplorationStrategyParameters;
import byzzbench.simulator.repository.CampaignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit test specifying ScenarioRunner sequencing: execute minEvents steps, then trigger GST,
 * then execute gstGracePeriod further steps.
 * <p>
 * This is a specification test that will fail until ScenarioRunner is updated to satisfy it.
 */
class ScenarioRunnerTest {

    private ByzzBenchConfig byzzBenchConfig;
    private CampaignRepository campaignRepository;
    private ScenarioService scenarioService;
    private ExplorationStrategyService explorationStrategyService;

    @BeforeEach
    void setup() {
        byzzBenchConfig = mock(ByzzBenchConfig.class);
        // Do not persist schedules by default for this unit test
        when(byzzBenchConfig.getSaveSchedules()).thenReturn(ByzzBenchConfig.SaveScheduleMode.NONE);
        campaignRepository = mock(CampaignRepository.class);
        scenarioService = mock(ScenarioService.class);
        explorationStrategyService = mock(ExplorationStrategyService.class);
    }

    @Test
    @DisplayName("ScenarioRunner should run minEvents steps, trigger GST, then run gstGracePeriod steps")
    void scenarioRunner_minEvents_thenGST_thenGracePeriod() {
        // Given a campaign termination policy
        long minEvents = 3;
        long gstGrace = 2;

        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setMinEvents(minEvents);
        terminationConfig.setGstGracePeriod(gstGrace);

        Campaign campaign = new Campaign();
        campaign.setTermination(terminationConfig);

        // Real schedule to count actions
        Schedule schedule = new Schedule(ScenarioParameters.builder().scenarioId("test").randomSeed(0L).build());

        // A mocked scenario with a stateful transport for GST
        Scenario scenario = mock(Scenario.class, RETURNS_DEEP_STUBS);
        when(scenario.getSchedule()).thenReturn(schedule);
        when(scenario.invariantsHold()).thenReturn(true);
        when(scenario.unsatisfiedInvariants()).thenReturn(new java.util.TreeSet<>());

        // Stateful GST toggling for transport
        AtomicBoolean gst = new AtomicBoolean(false);
        var transport = Mockito.mock(byzzbench.simulator.transport.Transport.class);
        when(transport.isGlobalStabilizationTime()).thenAnswer(inv -> gst.get());
        doAnswer(inv -> {
            gst.set(true);
            return null;
        }).when(transport).globalStabilizationTime();
        when(scenario.getTransport()).thenReturn(transport);

        // An exploration strategy that emits exactly minEvents actions before GST, returns empty to cause GST,
        // then emits exactly gstGracePeriod actions after GST, and finally returns empty.
        class TestExplorationStrategy extends ExplorationStrategy {
            private long pre = 0;
            private long post = 0;

            public TestExplorationStrategy() {
                super();
            }

            @Override
            public void initializeScenario(Scenario sc) { /* no-op */ }

            @Override
            public Optional<Action> scheduleNext(Scenario sc) {
                // Before GST, produce actions until we reach minEvents
                if (!gst.get()) {
                    if (pre < minEvents) {
                        Action a = mock(Action.class);
                        // Append directly so ScenarioRunner sees schedule length increasing
                        schedule.appendAction(a);
                        pre++;
                        return Optional.of(a);
                    } else {
                        // Stop producing to force GST
                        return Optional.empty();
                    }
                }
                // After GST, produce gstGracePeriod actions then stop
                if (post < gstGrace) {
                    Action a = mock(Action.class);
                    schedule.appendAction(a);
                    post++;
                    return Optional.of(a);
                }
                return Optional.empty();
            }

            @Override
            public void reset() { /* no-op */ }

            @Override
            public void loadSchedulerParameters(ExplorationStrategyParameters parameters) { /* no-op */ }
        }

        ExplorationStrategy strategy = new TestExplorationStrategy();

        // And a CampaignService to host the ScenarioRunner
        CampaignService campaignService = new CampaignService(byzzBenchConfig, campaignRepository, scenarioService, explorationStrategyService);
        CampaignService.ScenarioRunner runner = campaignService.new ScenarioRunner(campaign, scenario, strategy);

        // When we run the scenario
        runner.run();

        // Then: it should have triggered GST once after running minEvents actions
        assertTrue(gst.get(), "Global Stabilization Time should have been triggered");

        // And the total number of actions should be minEvents + gstGracePeriod
        assertEquals(minEvents + gstGrace, schedule.getActions().size(),
                "ScenarioRunner should execute minEvents, then after GST execute gstGracePeriod additional steps");
    }
}
