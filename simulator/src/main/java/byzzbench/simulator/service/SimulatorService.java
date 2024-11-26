package byzzbench.simulator.service;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.scheduler.EventDecision;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.messages.MessageWithRound;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for running the simulator.
 * <p>
 * This service is responsible for running the simulator with the selected
 * scenario.
 */
@Getter
@Service
@RequiredArgsConstructor
@Log
public class SimulatorService {
    private final ByzzBenchConfig byzzBenchConfig;
    private final ScenarioService scenarioService;
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    private SimulatorServiceMode mode = SimulatorServiceMode.STOPPED;
    private boolean shouldStop = false;
    private Scenario scenario;
    private String scenarioId;
    private JsonNode scenarioParams;

    @EventListener(ApplicationReadyEvent.class)
    @DependsOn("simulatorApplication")
    void onStartup() {
        this.scenarioId = this.byzzBenchConfig.getScenario().getId();
        this.scenarioParams = JsonNodeFactory.instance.objectNode();
        this.changeScenario(byzzBenchConfig.getScenario().getId(), JsonNodeFactory.instance.objectNode());

        if (this.byzzBenchConfig.isAutostart()) {
            this.start();
        }
    }

    /**
     * Changes the scenario to the scenario with the given ID.
     *
     * @param scenarioId The ID of the scenario to change to.
     * @param params     The parameters for the scenario.
     */
    public void changeScenario(String scenarioId, JsonNode params) {
        this.scenarioId = scenarioId;
        this.scenarioParams = params;
        this.resetScenario();
    }

    /**
     * Creates a new scenario with the previously set scenario ID and parameters, replacing the current scenario.
     */
    public void resetScenario() {
        if (this.scenario != null) {
            // serialize it to file
            Path root = byzzBenchConfig.getOutputPathForThisRun();
            // create a directory "xyz" in the root path
            String currentTimeWithMillis = String.format("%d%09d", Instant.now().getEpochSecond(), Instant.now().getNano());
            int scenarioIndex = this.scenarioService.getScenarios().size();
            Path scenarioPath = root.resolve(String.format("%s-%d", this.scenario.getId(), scenarioIndex));
            System.out.println("Scenario path: " + scenarioPath);
            try {
                Files.createDirectories(scenarioPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.scenario = this.scenarioService.generateScenario(this.scenarioId, this.scenarioParams);
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
        this.executor.submit(() -> {
            this.mode = SimulatorServiceMode.RUNNING;
            // reset the scenario to ensure that the scenario is in a clean state
            this.changeScenario(this.getScenarioId(), this.getScenarioParams());

            int numTerm = 0;
            int numMaxedOut = 0;
            int numErr = 0;
            try {
                // run the scenario until the stop flag is set
                while (!this.shouldStop) {
                    int scenarioId = this.scenarioService.getScenarios().size() + 1;
                    System.out.println("Running scenario #" + scenarioId);

                    try {
                        while (true) {
                            this.invokeScheduleNext();
                            long numEvents = this.scenario.getSchedule().getEvents().size();
                            long terminationSamplingFreq = this.byzzBenchConfig.getScenario().getTermination().getSamplingFrequency();
                            boolean shouldCheckTermination = numEvents % terminationSamplingFreq == 0;

                            // if the invariants do not hold, terminate the run
                            if (!this.scenario.invariantsHold()) {
                                log.info("Invariants do not hold, terminating. . .");
                                var unsatisfiedInvariants = this.scenario.unsatisfiedInvariants();
                                this.scenario.getSchedule().finalizeSchedule(unsatisfiedInvariants);
                                numTerm++;
                                break;
                            }

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

                                if (numEvents >= byzzBenchConfig.getScenario().getTermination().getMinEvents()
                                        && currentRound >= byzzBenchConfig.getScenario().getTermination().getMinRounds()) {
                                    log.info("Reached min # of events and rounds for this run, terminating. . .");
                                    numMaxedOut++;
                                    break;
                                }
                            }

                        }

                    } catch (Exception e) {
                        System.out.println("Error in schedule " + scenarioId + ": " + e);
                        e.printStackTrace();
                        numErr += 1;
                    }

                    this.resetScenario();
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

    public void invokeScheduleNext() throws Exception {
        Optional<EventDecision> decisionOptional = this.scenario.getScheduler().scheduleNext(this.scenario);
        if (decisionOptional.isPresent()) {
            //EventDecision decision = decisionOptional.get();
        } else {
            log.info("Couldn't schedule action");
        }
    }

    public enum SimulatorServiceMode {
        STOPPED, RUNNING
    }
}
