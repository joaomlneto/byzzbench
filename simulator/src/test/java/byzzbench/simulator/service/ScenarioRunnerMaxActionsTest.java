package byzzbench.simulator.service;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.config.TerminationConfig;
import byzzbench.simulator.domain.Campaign;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.exploration_strategy.ExplorationStrategy;
import byzzbench.simulator.exploration_strategy.ExplorationStrategyParameters;
import byzzbench.simulator.exploration_strategy.random.RandomExplorationStrategy;
import byzzbench.simulator.protocols.hbft.HbftJavaScenario;
import byzzbench.simulator.repository.CampaignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verify that when max actions (minEvents) is set to a small number (e.g., 5),
 * the ScenarioRunner delivers exactly 5 actions overall and then terminates.
 */
class ScenarioRunnerMaxActionsTest {

    private ByzzBenchConfig byzzBenchConfig;
    private CampaignRepository campaignRepository;
    private ScenarioService scenarioService;
    private ExplorationStrategyService explorationStrategyService;

    @BeforeEach
    void setup() {
        byzzBenchConfig = mock(ByzzBenchConfig.class);
        when(byzzBenchConfig.getSaveSchedules()).thenReturn(ByzzBenchConfig.SaveScheduleMode.NONE);
        campaignRepository = mock(CampaignRepository.class);
        scenarioService = mock(ScenarioService.class);
        explorationStrategyService = mock(ExplorationStrategyService.class);
    }

    @Test
    @DisplayName("ScenarioRunner should deliver exactly 5 actions (Faulty-Safety) when minEvents=5 and gstGracePeriod=0")
    void scenarioRunner_delivers_exactly_five_actions() {
        // Termination policy: exactly 5 actions, no grace after GST
        long minEvents = 5;
        long gstGrace = 0;

        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setMinEvents(minEvents);
        terminationConfig.setGstGracePeriod(gstGrace);

        Campaign campaign = new Campaign();
        campaign.setTermination(terminationConfig);

        // Create a real Faulty-Safety scenario with deterministic parameters
        Schedule schedule = new Schedule(ScenarioParameters.builder()
                .scenarioId("faulty-safety")
                .randomSeed(0L)
                .numClients(1)
                .numReplicas(4)
                .build());
        Scenario scenario = new HbftJavaScenario(schedule);

        ExplorationStrategy strategy = new RandomExplorationStrategy();
        ExplorationStrategyParameters strategyParameters = new ExplorationStrategyParameters();
        strategyParameters.setRandomSeed(0L);
        strategy.loadParameters(strategyParameters);

        // Run the scenario
        CampaignService campaignService = new CampaignService(byzzBenchConfig, campaignRepository, scenarioService, explorationStrategyService);
        CampaignService.ScenarioRunner runner = campaignService.new ScenarioRunner(campaign, scenario, strategy);
        runner.run();

        // Assert exactly 5 actions were delivered overall
        assertEquals(minEvents, scenario.getSchedule().getLength(),
                "ScenarioRunner should deliver exactly 5 actions when minEvents=5 and gstGracePeriod=0");
    }

    @Test
    @DisplayName("ScenarioRunner should deliver exactly 10 actions when minEvents=5 and gstGracePeriod=5")
    void scenarioRunner_delivers_exactly_five_actions_after_gst() {
        // Termination policy: 5 actions, 5 action grace after GST
        long minEvents = 5;
        long gstGrace = 5;

        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setMinEvents(minEvents);
        terminationConfig.setGstGracePeriod(gstGrace);

        Campaign campaign = new Campaign();
        campaign.setTermination(terminationConfig);

        // Create a real Faulty-Safety scenario with deterministic parameters
        Schedule schedule = new Schedule(ScenarioParameters.builder()
                .scenarioId("faulty-safety")
                .randomSeed(0L)
                .numClients(1)
                .numReplicas(4)
                .build());
        schedule.setCampaign(campaign);
        Scenario scenario = new HbftJavaScenario(schedule);

        //ensure scenario.invariantsHold() always return true

        ExplorationStrategy strategy = new RandomExplorationStrategy();
        ExplorationStrategyParameters strategyParameters = new ExplorationStrategyParameters();
        strategyParameters.setRandomSeed(0L);
        strategy.loadParameters(strategyParameters);

        // Run the scenario
        CampaignService campaignService = new CampaignService(byzzBenchConfig, campaignRepository, scenarioService, explorationStrategyService);
        CampaignService.ScenarioRunner runner = campaignService.new ScenarioRunner(campaign, scenario, strategy);
        runner.run();

        // Assert exactly 5 actions were delivered overall
        assertEquals(minEvents + gstGrace, scenario.getSchedule().getLength(),
                "ScenarioRunner should deliver exactly 10 actions when minEvents=5 and gstGracePeriod=5");
    }
}
