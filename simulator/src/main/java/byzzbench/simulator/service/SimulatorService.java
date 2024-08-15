package byzzbench.simulator.service;

import byzzbench.simulator.ScenarioExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.Serializable;
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
    private final SchedulesService schedulesService;
    private final ScenarioFactoryService scenarioFactoryService;
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    private SimulatorServiceMode mode = SimulatorServiceMode.STOPPED;
    private boolean shouldStop = false;
    private ScenarioExecutor<? extends Serializable> scenarioExecutor;

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
        //this.scenarioExecutor.setupScenario();
        //this.scenarioExecutor.runScenario();
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

            try {
                // run the scenario until the stop flag is set
                while (!this.shouldStop) {
                    int scenarioId = this.schedulesService.getSchedules().size() + 1;
                    System.out.println("Running scenario #" + scenarioId);
                    this.scenarioExecutor.runScenario();

                    // run the scenario for the given number of events
                    for (int i = 0; i < numActionsPerRun; i++) {
                        System.out.println("Running action #" + i + "/" + numActionsPerRun);
                        this.scenarioExecutor.getScheduler().scheduleNext();
                    }

                    System.out.println("Scenario #" + scenarioId + " completed");
                    this.scenarioExecutor.reset();

                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                this.mode = SimulatorServiceMode.STOPPED;
            }
        });
    }

    public enum SimulatorServiceMode {
        STOPPED, RUNNING
    }
}
