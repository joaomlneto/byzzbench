package byzzbench.simulator.service;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.domain.Action;
import byzzbench.simulator.domain.Campaign;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.exploration_strategy.ExplorationStrategy;
import byzzbench.simulator.repository.CampaignRepository;
import byzzbench.simulator.state.DeadlockPredicate;
import byzzbench.simulator.state.ErroredPredicate;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Log
@DependsOn("explorationStrategyService")
public class CampaignService {
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    private final ByzzBenchConfig byzzBenchConfig;
    private final CampaignRepository campaignRepository;
    private final ScenarioService scenarioService;
    private final ExplorationStrategyService explorationStrategyService;

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
        for (var campaignConfig : byzzBenchConfig.getCampaigns()) {
            Campaign cfgCampaign = Campaign.fromConfig(campaignConfig);
            this.registerNewCampaign(cfgCampaign);
            if (byzzBenchConfig.isAutostart() && campaignConfig.isAutoStart()) {
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

        @Override
        public void run() {
            log.info(String.format("Starting campaign %d with %d scenarios%n",
                    campaign.getCampaignId(), campaign.getNumScenarios()));
            boolean running = true;
            try {
                long numScenarios = campaign.getNumScenarios();
                ExplorationStrategy explorationStrategy = campaign.getExplorationStrategy();

                // run the scenario until the stop flag is set
                for (int i = 0; running && i < numScenarios; i++) {
                    log.info(String.format("Running scenario %d/%d%n", i + 1, numScenarios));

                    ScenarioParameters scenarioParams = campaign.generateScenarioParameters();
                    Scenario scenario = scenarioService.generateScenario(scenarioParams, this.campaign);
                    explorationStrategy.ensureScenarioInitialized(scenario);
                    ScenarioRunner r = new ScenarioRunner(campaign, scenario, explorationStrategy);
                    r.run();
                }
            } catch (Exception e) {
                log.severe("Error running campaign: " + e);
                e.printStackTrace();
            } finally {
                log.info(String.format("Campaign %d finished: %d terminated, %d maxed out, %d errored%n",
                        campaign.getCampaignId(), this.campaign.getNumTerm(), this.campaign.getNumMaxedOut(), this.campaign.getNumErr()));
                running = false;
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
                // initialize the exploration_strategy with the scenario
                explorationStrategy.ensureScenarioInitialized(currentScenario);

                // main scheduling loop
                while (true) {
                    Optional<Action> decision = explorationStrategy.scheduleNext(currentScenario);
                    log.info("Decision: " + decision);

                    // if the exploration_strategy did not make a decision, and we're before GST, trigger GST!
                    if (decision.isEmpty() && !currentScenario.getTransport().isGlobalStabilizationTime()) {
                        currentScenario.getTransport().globalStabilizationTime();
                        continue;
                    }

                    // if the exploration_strategy did not make a decision, and we're after GST, terminate the run
                    if (decision.isEmpty()) {
                        log.info("We're after GST and still no events!!");
                        currentScenario.getSchedule().finalizeSchedule(Set.of(new DeadlockPredicate(currentScenario)));
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

                    //log.info("Decision: " + decision.get());
                    //log.info("Num events: " + numEvents);
                    //log.info("Should check termination: " + shouldCheckTermination);

                    if (shouldCheckTermination) {
                        /*
                        OptionalLong maxDeliveredRound = currentScenario.getTransport()
                                .getEventsInState(Event.Status.DELIVERED)
                                .stream()
                                .filter(MessageWithRound.class::isInstance)
                                .map(MessageWithRound.class::cast)
                                .mapToLong(MessageWithRound::getByzzFuzzRound)
                                .max();

                        OptionalLong minQueuedRound = currentScenario.getTransport()
                                .getEventsInState(Event.Status.QUEUED)
                                .stream()
                                .filter(MessageWithRound.class::isInstance)
                                .map(MessageWithRound.class::cast)
                                .mapToLong(MessageWithRound::getByzzFuzzRound)
                                .min();
                        long currentRound = minQueuedRound.orElse(maxDeliveredRound.orElse(0));
                        */

                        //log.info("Current round: " + currentRound);
                        //log.info("Max round: " + maxDeliveredRound.orElse(0));
                        //log.info("Min round: " + this.getCampaign().getTermination().getMinRounds());

                        if (numEvents >= this.getCampaign().getTermination().getMinEvents()
                            /*&& currentRound >= this.getCampaign().getTermination().getMinRounds()*/) {
                            log.info("Reached min # of events or rounds for this run, terminating. . .");
                            this.result = ScenarioExecutionResult.CORRECT;
                            this.finalizeSchedule(currentScenario, Collections.emptySet());
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                log.info("Error in schedule. " + e);
                e.printStackTrace();
                this.finalizeSchedule(currentScenario, Set.of(new ErroredPredicate(currentScenario)));
                this.result = ScenarioExecutionResult.ERRORED;
            } finally {
                this.campaign.processScenarioResult(this.result);
                campaignRepository.save(campaign);
            }
        }
    }
}
