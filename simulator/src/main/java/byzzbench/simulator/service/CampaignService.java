package byzzbench.simulator.service;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.domain.Action;
import byzzbench.simulator.domain.Campaign;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.exploration_strategy.ExplorationStrategy;
import byzzbench.simulator.repository.CampaignRepository;
import byzzbench.simulator.repository.ScheduleRepository;
import byzzbench.simulator.state.ErroredPredicate;
import byzzbench.simulator.state.LivenessPredicate;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.messages.MessageWithRound;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Log
public class CampaignService {
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    private final ByzzBenchConfig byzzBenchConfig;
    private final CampaignRepository campaignRepository;
    private final ScenarioService scenarioService;
    private final ExplorationStrategyService explorationStrategyService;
    private final ScheduleRepository scheduleRepository;

    private final Map<Long, Campaign> activeCampaigns = new HashMap<>();

    /**
     * Register a new campaign in the database
     *
     * @param campaign the campaign to register
     */
    public void registerNewCampaign(Campaign campaign) {
        this.campaignRepository.save(campaign);
    }

    /**
     * Get a campaign by its id, loading it from the database if necessary
     *
     * @param campaignId the id of the campaign
     * @return the campaign
     * @throws NoSuchElementException if the campaign does not exist
     */
    public Campaign getCampaign(long campaignId) {
        return activeCampaigns.putIfAbsent(campaignId, campaignRepository.findByCampaignId(campaignId).orElseThrow());
    }

    /**
     * Run a campaign in a new thread
     *
     * @param campaign the campaign to run
     */
    public void runCampaign(Campaign campaign) {
        CampaignRunner runner = new CampaignRunner(campaign);
        this.executor.submit(runner);
    }

    @PostConstruct
    public void init() {
        // read campaign configs from application.yml and run them
        if (byzzBenchConfig.isAutostart()) {
            for (var campaignConfig : byzzBenchConfig.getCampaigns()) {
                Campaign cfgCampaign = Campaign.fromConfig(campaignConfig);
                this.registerNewCampaign(cfgCampaign);
                this.runCampaign(cfgCampaign);
            }
        }
    }

    public enum ScenarioExecutionResult {
        /**
         * The scenario reached the maximum number of steps without finding a bug.
         */
        CORRECT,
        /**
         * The scenario terminated because it found a bug (invariant violation).
         */
        TERMINATED,
        /**
         * The scenario encountered an error during execution.
         */
        ERRORED
    }

    /**
     * Thread class to run a campaign
     */
    @RequiredArgsConstructor
    public class CampaignRunner implements Runnable {
        private final Campaign campaign;

        /**
         * Number of terminated scenarios (bug found)
         */
        private long numTerm = 0;

        // statistics
        /**
         * Number of maxed out scenarios (no bugs found, reached max steps)
         */
        private long numMaxedOut = 0;

        /**
         * Number of errored scenarios (simulation error)
         */
        private long numErr = 0;

        private boolean running = false;

        @Override
        public void run() {
            try {
                log.info(String.format("Starting campaign %d with %d scenarios%n",
                        campaign.getCampaignId(), campaign.getNumScenarios()));

                this.running = true;

                System.out.println("Hi im here");

                long numScenarios = campaign.getNumScenarios();
                System.out.println("Number of scenarios: " + numScenarios);
                String scenarioId = campaign.getScenarioId();
                System.out.println("Scenario Factory ID: " + scenarioId);
                String explorationStrategyId = campaign.getExplorationStrategyId();
                System.out.println("Exploration Strategy ID: " + explorationStrategyId);
                ExplorationStrategy explorationStrategy = explorationStrategyService.getExplorationStrategy(explorationStrategyId);
                System.out.println("Exploration Strategy ID: " + explorationStrategyId);

                this.numTerm = 0;
                this.numMaxedOut = 0;
                this.numErr = 0;

                // run the scenario until the stop flag is set
                for (int i = 0; this.running && i < numScenarios; i++) {
                    log.info(String.format("Running scenario %d/%d%n", i + 1, numScenarios));

                    ScenarioParameters scenarioParams = campaign.getScenarioParameters();
                    Scenario scenario = scenarioService.generateScenario(scenarioParams);

                    Runnable r = new ScenarioRunner(campaign, scenario, explorationStrategy);
                    r.run();
                }
            } catch (Exception e) {
                log.severe("Error running campaign: " + e);
                e.printStackTrace();
            } finally {
                log.info(String.format("Campaign %d finished: %d terminated, %d maxed out, %d errored%n",
                        campaign.getCampaignId(), numTerm, numMaxedOut, numErr));
                this.running = false;
            }

        }
    }

    @RequiredArgsConstructor
    @Getter
    public class ScenarioRunner implements Runnable {
        private final Campaign campaign;
        private final Scenario currentScenario;
        private final ExplorationStrategy explorationStrategy;
        private ScenarioExecutionResult result;

        /**
         * FIXME: saving schedule according to selected policy logic should be in the {@link ScenarioService}
         */
        private void finalizeSchedule(Scenario scenario, Set<ScenarioPredicate> brokenInvariants) {
            scenario.getSchedule().finalizeSchedule(brokenInvariants);
            scenario.getSchedule().setCampaign(this.campaign);

            // save the schedule according to the selected policy
            if ((byzzBenchConfig.getSaveSchedules() == ByzzBenchConfig.SaveScheduleMode.ALL) ||
                    (byzzBenchConfig.getSaveSchedules() == ByzzBenchConfig.SaveScheduleMode.BUGGY
                            && !brokenInvariants.isEmpty())) {
                scenarioService.storeSchedule(scenario.getSchedule().getScheduleId());
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Optional<Action> decision = explorationStrategy.scheduleNext(currentScenario);
                    System.out.println("Decision: " + decision);

                    // if the exploration_strategy did not make a decision, and we're before GST, set GST!
                    if (decision.isEmpty() && !currentScenario.getTransport().isGlobalStabilizationTime()) {
                        currentScenario.getTransport().globalStabilizationTime();
                        continue;
                    }

                    if (decision.isEmpty()) {
                        System.out.println("We're after GST and still no events!!");
                        currentScenario.getSchedule().finalizeSchedule(Set.of(new LivenessPredicate()));
                        currentScenario.getSchedule().setCampaign(campaign);
                        scenarioService.storeSchedule(currentScenario.getSchedule().getScheduleId());
                        this.result = ScenarioExecutionResult.TERMINATED;
                        break;
                    }

                    long numEvents = currentScenario.getSchedule().getActions().size();
                    long terminationSamplingFreq = this.getCampaign().getTermination().getSamplingFrequency();
                    boolean shouldCheckTermination = (numEvents % terminationSamplingFreq) == 0;

                    // if the invariants do not hold, terminate the run
                    if (!currentScenario.invariantsHold()) {
                        log.info("Invariants do not hold, terminating. . .");
                        this.result = ScenarioExecutionResult.TERMINATED;
                        this.finalizeSchedule(currentScenario, currentScenario.unsatisfiedInvariants());
                        break;
                    }

                    System.out.println("Decision: " + decision.get());
                    System.out.println("Num events: " + numEvents);
                    System.out.println("Should check termination: " + shouldCheckTermination);

                    if (shouldCheckTermination) {
                        OptionalLong maxDeliveredRound = currentScenario.getTransport()
                                .getEventsInState(Event.Status.DELIVERED)
                                .stream()
                                .filter(MessageWithRound.class::isInstance)
                                .map(MessageWithRound.class::cast)
                                .mapToLong(MessageWithRound::getRound)
                                .max();

                        OptionalLong minQueuedRound = currentScenario.getTransport()
                                .getEventsInState(Event.Status.QUEUED)
                                .stream()
                                .filter(MessageWithRound.class::isInstance)
                                .map(MessageWithRound.class::cast)
                                .mapToLong(MessageWithRound::getRound)
                                .min();
                        long currentRound = minQueuedRound.orElse(maxDeliveredRound.orElse(0));

                        System.out.println("Current round: " + currentRound);
                        System.out.println("Max round: " + maxDeliveredRound.orElse(0));
                        System.out.println("Min round: " + this.getCampaign().getTermination().getMinRounds());

                        if (numEvents >= this.getCampaign().getTermination().getMinEvents()
                                && currentRound >= this.getCampaign().getTermination().getMinRounds()) {
                            log.info("Reached min # of events or rounds for this run, terminating. . .");
                            this.result = ScenarioExecutionResult.CORRECT;
                            this.finalizeSchedule(currentScenario, Collections.emptySet());
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error in schedule. " + e);
                e.printStackTrace();
                this.finalizeSchedule(currentScenario, Set.of(new ErroredPredicate()));
                this.result = ScenarioExecutionResult.ERRORED;
            }
        }
    }
}
