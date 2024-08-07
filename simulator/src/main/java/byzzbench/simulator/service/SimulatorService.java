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
    private final SimulatorServiceMode mode = SimulatorServiceMode.STOPPED;
    private final boolean shouldStop = false;
    private ScenarioExecutor<? extends Serializable> scenarioExecutor;

  @EventListener(ApplicationReadyEvent.class)
  void onStartup() {
    log.info("Starting the simulator service");
    this.changeScenario("xrpl");
    log.info("Simulator service started");
  }

  public void changeScenario(String id) {
    this.scenarioExecutor = this.scenarioFactoryService.getScenario(id);
    this.scenarioExecutor.setupScenario();
    this.scenarioExecutor.runScenario();
    // this.scenarioExecutor.reset();
  }
}
