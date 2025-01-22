package byzzbench.simulator.service;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.protocols.event_driven_hotstuff.EDHSScenarioState;
import byzzbench.simulator.protocols.event_driven_hotstuff.EDHotStuffScenario;
import byzzbench.simulator.scheduler.EventDecision;
import byzzbench.simulator.state.AgreementPredicate;
import byzzbench.simulator.state.ErroredPredicate;
import byzzbench.simulator.state.LivenessPredicate;
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
import java.util.OptionalLong;
import java.util.Set;
import java.util.*;
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
    private Path scenarioPath;
    private int scenarioIndex = 0;
    private final ArrayList<String> failedScenarios = new ArrayList<>();
    private final ArrayList<String> errorScenarios = new ArrayList<>();
    private final ArrayList<EDHSScenarioState> scenarioStates = new ArrayList<>();

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
            this.scenarioIndex = this.scenarioService.getScenarios().size();
            scenarioPath = root.resolve(String.format("%s-%d", this.scenario.getId(), scenarioIndex));
            System.out.println("Scenario path: " + scenarioPath);
            try {
                Files.createDirectories(scenarioPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if(scenario instanceof EDHotStuffScenario edHotStuffScenario)
                scenarioStates.add(edHotStuffScenario.getState());
        }
        this.scenario = this.scenarioService.generateScenario(this.scenarioId, this.scenarioParams);
        if(this.scenario instanceof EDHotStuffScenario edHotStuffScenario)
            edHotStuffScenario.log(byzzBenchConfig.isSmallScope() ? "SMALL-SCOPE mutations" : "ANY-SCOPE mutations");
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

            int agreementViolations = 0;
            int livenessViolations = 0;
            int bothViolations = 0;

            int numTerm = 0;
            int numMaxedOut = 0;
            int numErr = 0;
            try {
                // run the scenario until the stop flag is set
                for (int i = 0; !this.shouldStop && i < this.byzzBenchConfig.getNumScenarios(); i++) {
                    int scenarioId = this.scenarioService.getScenarios().size() + 1;
                    System.out.println("Running scenario #" + scenarioId);

                    try {
                        while (true) {
                            this.scenario.getScheduler().scheduleNext(this.scenario);

                            long numEvents = this.scenario.getSchedule().getEvents().size();
                            long terminationSamplingFreq = this.byzzBenchConfig.getScenario().getTermination().getSamplingFrequency();
                            boolean shouldCheckTermination = (numEvents % terminationSamplingFreq) == 0;

                            // if the invariants do not hold, terminate the run
                            if (!this.scenario.invariantsHold()) {
                                log.info("Invariants do not hold, terminating. . .");
                                var unsatisfiedInvariants = this.scenario.unsatisfiedInvariants();
                                boolean agreementViolation = false;
                                boolean livenessViolation = false;
                                for(var inv : unsatisfiedInvariants) {
                                    switch (inv) {
                                        case AgreementPredicate ignored -> agreementViolation = true;
                                        case LivenessPredicate ignored -> livenessViolation = true;
                                        default -> {}
                                    }
                                }
                                if(agreementViolation && livenessViolation) bothViolations++;
                                if(agreementViolation) agreementViolations++;
                                if(livenessViolation) livenessViolations++;
                                this.scenario.getSchedule().finalizeSchedule(unsatisfiedInvariants);
                                numTerm++;
                                failedScenarios.add(String.valueOf(this.scenarioIndex));
                                if(scenario instanceof EDHotStuffScenario edHotStuffScenario) {
                                    Files.write(scenarioPath.resolve("logs.txt"), String.join("\n", edHotStuffScenario.getLogs()).getBytes());
                                    edHotStuffScenario.getState().setAgreementViolation(agreementViolation);
                                    edHotStuffScenario.getState().setLivenessViolation(livenessViolation);
                                }
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
                                        || currentRound >= byzzBenchConfig.getScenario().getTermination().getMinRounds()) {
                                    log.info("Reached min # of events or rounds for this run, terminating. . .");
                                    numMaxedOut++;
                                    scenario.getSchedule().finalizeSchedule();
                                    break;
                                }
                            }

                        }

                    } catch (Exception e) {
                        System.out.println("Error in schedule " + scenarioId + ": " + e);
                        e.printStackTrace();
                        errorScenarios.add(String.valueOf(this.scenarioIndex));
                        if(scenario instanceof EDHotStuffScenario edHotStuffScenario) {
                            edHotStuffScenario.log(e.getMessage());
                            edHotStuffScenario.log(Arrays.toString(e.getStackTrace()));
                            Files.write(scenarioPath.resolve("logs.txt"), String.join("\n", edHotStuffScenario.getLogs()).getBytes());
                        }
                        scenario.getSchedule().finalizeSchedule(Set.of(new ErroredPredicate()));
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
                System.out.println("Liveness violations: " + livenessViolations);
                System.out.println("Agreement violations: " + agreementViolations);
                System.out.println("Both violations: " + bothViolations);
                System.out.println("failedScenarios: " + String.join(", ", failedScenarios));
                System.out.println("errorScenarios: " + String.join(", ", errorScenarios));
                try {
                    String summary =
                            "byzzbench config: " + byzzBenchConfig + "\n\n" +
                            "count: " + scenarioStates.size() + "\n" +
                            "error runs: " + String.join(", ", errorScenarios) + "\n"  +
                            "error count: " + numErr + "\n"  +
                            "failed: " + String.join(", ", failedScenarios) + "\n"  +
                            "liveness: " + livenessViolations + "\n" +
                            "agreement: " + agreementViolations + "\n" +
                            "both: " + bothViolations + "\n" +
                            "total failed: " + numTerm + "\n" +
                            "\nONLY-CORRECT-ASSUMPTIONS\n" +
                            "count: " + scenarioStates.stream().filter(s -> s.isValidAssumptions()).count() + "\n" +
                            "liveness: " + scenarioStates.stream().filter(s -> s.isValidAssumptions() && s.isLivenessViolation()).count() + "\n" +
                            "agreement " + scenarioStates.stream().filter(s -> s.isValidAssumptions() && s.isAgreementViolation()).count() + "\n" +
                            "both: " + scenarioStates.stream().filter(s -> s.isValidAssumptions() && s.isAgreementViolation() && s.isLivenessViolation()).count() + "\n" +
                            "\nONLY-INVALID-ASSUMPTIONS\n" +
                            "count: " + scenarioStates.stream().filter(s -> !s.isValidAssumptions()).count() + "\n" +
                            "liveness: " + scenarioStates.stream().filter(s -> !s.isValidAssumptions() && s.isLivenessViolation()).count() + "\n" +
                            "agreement " + scenarioStates.stream().filter(s -> !s.isValidAssumptions() && s.isAgreementViolation()).count() + "\n" +
                            "both: " + scenarioStates.stream().filter(s -> !s.isValidAssumptions() && s.isAgreementViolation() && s.isLivenessViolation()).count() + "\n"
                            + "\n";

                    String states = String.join("\n", scenarioStates.stream().map(EDHSScenarioState::toString).toList());
                    Files.write(byzzBenchConfig.getOutputPathForThisRun().resolve("states" + scenarioStates.size() + ".txt"), (summary + states).getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public enum SimulatorServiceMode {
        STOPPED, RUNNING
    }
}
