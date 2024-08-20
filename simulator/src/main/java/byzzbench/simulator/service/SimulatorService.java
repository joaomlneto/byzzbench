package byzzbench.simulator.service;

import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.TerminationCondition;
import byzzbench.simulator.schedule.Schedule;
import byzzbench.simulator.scheduler.EventDecision;
import byzzbench.simulator.transport.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Optional;
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
    private final int MAX_EVENTS_FOR_RUN = SimulatorConfig.MAX_EVENTS_FOR_RUN;
    private final int MAX_DROPPED_MESSAGES = SimulatorConfig.MAX_DROPPED_MESSAGES;
    private final int CHECK_TERMINATION_FREQ = SimulatorConfig.CHECK_TERMINATION_FREQ;
    private int droppedMessageCount;
    private final SchedulesService schedulesService;
    private final ScenarioFactoryService scenarioFactoryService;
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    private SimulatorServiceMode mode = SimulatorServiceMode.STOPPED;
    private boolean shouldStop = false;
    private ScenarioExecutor<? extends Serializable> scenarioExecutor;
    private TerminationCondition terminationCondition;

    @EventListener(ApplicationReadyEvent.class)
    void onStartup() {
        log.info("Starting the simulator service");
        this.changeScenario("xrpl");
        log.info("Simulator service started");
    }

    /**
     * Changes the scenario to the scenario with the given ID.
     *
     * @param id The ID of the scenario to change to.
     */
    public void changeScenario(String id) {
        this.scenarioExecutor = this.scenarioFactoryService.getScenario(id);
        this.scenarioExecutor.setupScenario();
        //this.scenarioExecutor.setupScenario();
        //this.scenarioExecutor.runScenario();
        this.droppedMessageCount = 0;
        this.scenarioExecutor.reset();
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
            this.changeScenario(this.scenarioExecutor.getId());
            this.terminationCondition = this.scenarioExecutor.getTerminationCondition();
            
            int numTerm = 0;
            int numMaxedOut = 0;
            try {
                // run the scenario until the stop flag is set
                while (!this.shouldStop) {
                    int num_events = 0;
                    this.droppedMessageCount = 0;
                    int scenarioId = this.schedulesService.getSchedules().size() + 1;
                    System.out.println("Running scenario #" + scenarioId);

                    boolean flag = true;
                    while (flag) {
                        this.invokeScheduleNext();
                        num_events += 1;

                        if ((num_events % CHECK_TERMINATION_FREQ == 0 && this.terminationCondition.shouldTerminate())) {
                            log.info("Termination condition has been satisfied for this run, terminating. . .");
                            flag = false;
                            numTerm += 1;
                        }

                        if (num_events > MAX_EVENTS_FOR_RUN) {
                            log.info("Reached max # of actions for this run, terminating. . .");
                            flag = false;
                            numMaxedOut += 1;
                        }
                    }

                    // run the scenario for the given number of events
                   /*  for (int i = 1; i < numActionsPerRun; i++) {
                        System.out.println("Running action " + i + "/" + numActionsPerRun);
                        this.scenarioExecutor.getScheduler().scheduleNext();
                    } */
                    this.scenarioExecutor.reset();
                    this.droppedMessageCount = 0;
                    this.scenarioExecutor.getScheduler().resetParameters();
                    this.terminationCondition = this.scenarioExecutor.getTerminationCondition();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                this.mode = SimulatorServiceMode.STOPPED;
                
                log.info("number of runs terminated by condition: " + numTerm);
                log.info("number of runs terminated by max actions: " + numMaxedOut);
            }
        });
    }

    public void invokeScheduleNext() throws Exception {
        Optional<EventDecision> decisionOptional = this.scenarioExecutor.getScheduler().scheduleNext();
        if (decisionOptional.isPresent()) {
            EventDecision decision = decisionOptional.get();
            if (decision.getDecision() == EventDecision.DecisionType.DROPPED) {
                this.droppedMessageCount += 1;
            }

            if (this.scenarioExecutor.getScheduler().isDropMessages() && this.droppedMessageCount >= MAX_DROPPED_MESSAGES ) {
                this.scenarioExecutor.getScheduler().stopDropMessages();
            }
        } else {
            log.info("Couldn't schedule action");
        }
    }

    private String convertEventListToString(Schedule<?> schedule) {
        String res = "schedule: \n ";
        for (Event event : s.getEvents()) {
            res += "eid: "+ event.getEventId() + " " + event.getSenderId() + " -> " + event.getRecipientId() + ", ";
        }
        return res;
    }

    public enum SimulatorServiceMode {
        STOPPED, RUNNING
    }
}
