package byzzbench.simulator.service;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.TerminationCondition;
import byzzbench.simulator.scheduler.EventDecision;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static byzzbench.simulator.config.SimulatorConfig.DEFAULT_SCENARIO;

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
    private final int MAX_EVENTS_FOR_RUN = SimulatorConfig.MAX_EVENTS_FOR_RUN;
    private final int MAX_DROPPED_MESSAGES = SimulatorConfig.MAX_DROPPED_MESSAGES;
    private final int CHECK_TERMINATION_FREQ = SimulatorConfig.CHECK_TERMINATION_FREQ;
    private final ScenarioService scenarioService;
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    private int droppedMessageCount;
    private SimulatorServiceMode mode = SimulatorServiceMode.STOPPED;
    private boolean shouldStop = false;
    private Scenario scenario;
    private TerminationCondition terminationCondition;
    private String scenarioId;
    private JsonNode scenarioParams;

    @EventListener(ApplicationReadyEvent.class)
    void onStartup() {
        this.changeScenario(DEFAULT_SCENARIO, JsonNodeFactory.instance.objectNode());
    }

    /**
     * Changes the scenario to the scenario with the given ID.
     *
     * @param scenarioId The ID of the scenario to change to.
     * @param params The parameters for the scenario.
     */
    public void changeScenario(String scenarioId, JsonNode params) {
        this.scenarioId = scenarioId;
        this.scenarioParams = params;
        this.resetScenario();
    }

    public void resetScenario() {
        this.scenario = this.scenarioService.generateScenario(this.scenarioId, this.scenarioParams);
        //this.scenario.setupScenario();
        this.droppedMessageCount = 0;
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
     *
     * @param numActionsPerRun The number of scheduler actions to run per run.
     */
    public void start(int numActionsPerRun) {
        // check if the simulator is already running
        if (this.mode == SimulatorServiceMode.RUNNING) {
            throw new IllegalStateException("The simulator is already running");
        }

        this.shouldStop = false;
        this.executor.submit(() -> {
            this.mode = SimulatorServiceMode.RUNNING;
            // reset the scenario to ensure that the scenario is in a clean state
            this.changeScenario(this.getScenarioId(), this.getScenarioParams());
            this.terminationCondition = this.scenario.getTerminationCondition();

            int numTerm = 0;
            int numMaxedOut = 0;
            int numErr = 0;
            try {
                // run the scenario until the stop flag is set
                while (!this.shouldStop) {
                    int num_events = 0;
                    this.droppedMessageCount = 0;
                    int scenarioId = this.scenarioService.getScenarios().size() + 1;
                    System.out.println("Running scenario #" + scenarioId);

                    boolean flag = true;
                        try {
                            while (flag) {
                                this.invokeScheduleNext();
                                num_events += 1;

                                if ((num_events % CHECK_TERMINATION_FREQ == 0 && this.terminationCondition.shouldTerminate())) {
                                    log.info("Termination condition has been satisfied for this run, terminating. . .");
                                    flag = false;
                                    numTerm += 1;
                                }

                                // if the invariants do not hold, terminate the run
                                if (!this.scenario.invariantsHold()) {
                                    log.info("Invariants do not hold, terminating. . .");
                                    var unsatisfiedInvariants = this.scenario.unsatisfiedInvariants();
                                    this.scenario.getSchedule().finalizeSchedule(unsatisfiedInvariants);
                                    break;
                                }

                                if (num_events > MAX_EVENTS_FOR_RUN) {
                                    log.info("Reached max # of actions for this run, terminating. . .");
                                    flag = false;
                                    numMaxedOut += 1;
                                }
                            }

                        } catch (Exception e) {
                            System.out.println("Error in schedule " + scenarioId + ": " + e);
                            e.printStackTrace();
                            flag = false;
                            numErr += 1;
                        }

                    // run the scenario for the given number of events
                   /*  for (int i = 1; i < numActionsPerRun; i++) {
                        System.out.println("Running action " + i + "/" + numActionsPerRun);
                        this.scenarioExecutor.getScheduler().scheduleNext();
                    } */
                    this.resetScenario();
                    this.droppedMessageCount = 0;
                    this.terminationCondition = this.scenario.getTerminationCondition();
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
            EventDecision decision = decisionOptional.get();
            if (decision.getDecision() == EventDecision.DecisionType.DROPPED) {
                this.droppedMessageCount += 1;
            }

            if (this.scenario.getScheduler().isDropMessages() && this.droppedMessageCount >= MAX_DROPPED_MESSAGES ) {
                this.scenario.getScheduler().stopDropMessages();
            }
        } else {
            log.info("Couldn't schedule action");
        }
    }

    public boolean optimalDecisionPresent() throws Exception{
        Optional<EventDecision> decisionOptional = this.scenario.getScheduler().scheduleNext(this.scenario);
        if (decisionOptional.isPresent()) {
            return true;
        } else {
            return false;
        }
    }

    public enum SimulatorServiceMode {
        STOPPED, RUNNING
    }
}
