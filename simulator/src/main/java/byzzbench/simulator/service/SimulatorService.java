package byzzbench.simulator.service;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.domain.Action;
import byzzbench.simulator.domain.Campaign;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.repository.CampaignRepository;
import byzzbench.simulator.repository.ScheduleRepository;
import byzzbench.simulator.scheduler.Scheduler;
import byzzbench.simulator.state.ErroredPredicate;
import byzzbench.simulator.state.LivenessPredicate;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service to manage active simulations.
 */
@Getter
@Service
@RequiredArgsConstructor
@Log
public class SimulatorService {
    private final ByzzBenchConfig byzzBenchConfig;
    private final ScenarioService scenarioService;
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    private final ScheduleRepository scheduleRepository;
    private final CampaignRepository campaignRepository;
    private final SchedulerService schedulerService;
    private SimulatorServiceMode mode = SimulatorServiceMode.STOPPED;
    private boolean shouldStop = false;
    private Scenario scenario;
    private String scenarioFactoryId;
    private long scenarioId;
    private ScenarioParameters scenarioParams;
    private Scheduler scheduler;

    private void finalizeSchedule(Set<ScenarioPredicate> brokenInvariants, Campaign campaign) {
        scenario.getSchedule().finalizeSchedule(brokenInvariants);
        scenario.getSchedule().setCampaign(campaign);

        // save the schedule according to the selected policy
        if ((byzzBenchConfig.getSaveSchedules() == ByzzBenchConfig.SaveScheduleMode.ALL) ||
                (byzzBenchConfig.getSaveSchedules() == ByzzBenchConfig.SaveScheduleMode.BUGGY
                        && !brokenInvariants.isEmpty())) {
            scheduleRepository.save(scenario.getSchedule());
        }

    }

    @EventListener(ApplicationReadyEvent.class)
    @DependsOn("simulatorApplication")
    void onStartup() {
        this.scenarioFactoryId = this.byzzBenchConfig.getScenario().getId();
        this.scenarioParams = ScenarioParameters.builder()
                .scenarioFactoryId(this.byzzBenchConfig.getScenario().getId())
                .randomSeed(1L)
                .build();
        this.changeScenario(this.scenarioFactoryId, this.scenarioParams);

        if (this.byzzBenchConfig.isAutostart()) {
            this.start();
        }

        this.scheduler = this.schedulerService.getScheduler(
                this.byzzBenchConfig.getScheduler().getId()
        );
    }

    /**
     * Changes the scenario to the scenario with the given ID.
     *
     * @param scenarioFactoryId The ID of the scenario to change to.
     * @param params            The parameters for the scenario.
     */
    public void changeScenario(String scenarioFactoryId, ScenarioParameters params) {
        this.scenarioFactoryId = scenarioFactoryId;
        this.scenarioParams = params;
        this.resetScenario();
    }

    /**
     * Creates a new scenario with the previously set scenario ID and parameters, replacing the current scenario.
     */
    public void resetScenario() {
        if (this.scenario != null) {
            this.scenarioService.removeScenario(this.scenarioId);
        }
        Schedule schedule = new Schedule(scenarioParams);
        scheduleRepository.save(schedule);
        this.scenarioId = this.scenarioService.generateScenario(schedule);
        this.scenario = this.scenarioService.getScenarios().get(this.scenarioId);
        this.scenario.runScenario();
    }

    /**
     * Stops the simulator.
     */
    public void stop() {
        // check if the simulator is already stopped
        if (this.mode == SimulatorServiceMode.STOPPED) {
            throw new IllegalStateException("The simulator is already stopped");
        }

        this.shouldStop = true;
    }

    /**
     * Starts the simulator with the given number of actions per run.
     */
    public void start() {
        // check if the simulator is already running
        if (this.mode == SimulatorServiceMode.RUNNING) {
            throw new IllegalStateException("The simulator is already running");
        }

        this.shouldStop = false;

        // create thread to execute the campaign
        this.executor.submit(() -> {
            this.mode = SimulatorServiceMode.RUNNING;
            long numScenarios = this.byzzBenchConfig.getNumScenarios();
            Campaign campaign = new Campaign();
            campaign.setNumScenarios(numScenarios);
            campaignRepository.save(campaign);

            // reset the scenario to ensure that the scenario is in a clean state
            this.changeScenario(this.getScenarioFactoryId(), this.getScenarioParams());

            int numTerm = 0;
            int numMaxedOut = 0;
            int numErr = 0;
            try {
                // run the scenario until the stop flag is set
                for (int i = 0; !this.shouldStop && i < numScenarios; i++) {
                    log.info(String.format("Running scenario %d/%d%n", i + 1, numScenarios));

                    try {
                        while (true) {
                            Optional<Action> decision = this.scheduler.scheduleNext(this.scenario);
                            System.out.println("Decision: " + decision);

                            // if the scheduler did not make a decision, and we're before GST, set GST!
                            if (decision.isEmpty() && !this.scenario.getTransport().isGlobalStabilizationTime()) {
                                this.scenario.getTransport().globalStabilizationTime();
                                continue;
                            }

                            if (decision.isEmpty()) {
                                System.out.println("We're after GST and still no events!!");
                                this.scenario.getSchedule().finalizeSchedule(Set.of(new LivenessPredicate()));
                                scenario.getSchedule().setCampaign(campaign);
                                scheduleRepository.save(scenario.getSchedule());
                                numTerm++;
                                break;
                            }

                            long numEvents = this.scenario.getSchedule().getActions().size();
                            long terminationSamplingFreq = this.byzzBenchConfig.getScenario().getTermination().getSamplingFrequency();
                            boolean shouldCheckTermination = (numEvents % terminationSamplingFreq) == 0;

                            // if the invariants do not hold, terminate the run
                            if (!this.scenario.invariantsHold()) {
                                log.info("Invariants do not hold, terminating. . .");
                                numTerm++;
                                this.finalizeSchedule(this.scenario.unsatisfiedInvariants(), campaign);
                                break;
                            }

                            System.out.println("Decision: " + decision.get());
                            System.out.println("Num events: " + numEvents);
                            System.out.println("Should check termination: " + shouldCheckTermination);

                            if (shouldCheckTermination) {
                                OptionalLong maxDeliveredRound = this.scenario.getTransport()
                                        .getEventsInState(Event.Status.DELIVERED)
                                        .stream()
                                        .filter(MessageWithRound.class::isInstance)
                                        .map(MessageWithRound.class::cast)
                                        .mapToLong(MessageWithRound::getRound)
                                        .max();

                                OptionalLong minQueuedRound = this.scenario.getTransport()
                                        .getEventsInState(Event.Status.QUEUED)
                                        .stream()
                                        .filter(MessageWithRound.class::isInstance)
                                        .map(MessageWithRound.class::cast)
                                        .mapToLong(MessageWithRound::getRound)
                                        .min();
                                long currentRound = minQueuedRound.orElse(maxDeliveredRound.orElse(0));

                                System.out.println("Current round: " + currentRound);
                                System.out.println("Max round: " + maxDeliveredRound.orElse(0));
                                System.out.println("Min round: " + byzzBenchConfig.getScenario().getTermination().getMinRounds());

                                if (numEvents >= byzzBenchConfig.getScenario().getTermination().getMinEvents()
                                        && currentRound >= byzzBenchConfig.getScenario().getTermination().getMinRounds()) {
                                    log.info("Reached min # of events or rounds for this run, terminating. . .");
                                    numMaxedOut++;
                                    this.finalizeSchedule(Collections.emptySet(), campaign);
                                    break;
                                }
                            }

                        }

                    } catch (Exception e) {
                        System.out.println("Error in schedule " + scenarioFactoryId + ": " + e);
                        e.printStackTrace();
                        this.finalizeSchedule(Set.of(new ErroredPredicate()), campaign);
                        numErr += 1;
                    } finally {
                        this.resetScenario();
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                this.mode = SimulatorServiceMode.STOPPED;
                System.out.println("number of runs terminated by condition: " + numTerm);
                System.out.println("number of runs terminated by max actions: " + numMaxedOut);
                System.out.println("number of runs halted by error: " + numErr);
            }
        });
    }

    public enum SimulatorServiceMode {
        STOPPED, RUNNING
    }
}
