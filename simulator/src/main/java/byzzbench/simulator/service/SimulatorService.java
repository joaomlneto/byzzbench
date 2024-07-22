package byzzbench.simulator.service;

import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.protocols.XRPL.XRPLScenarioExecutor;
import byzzbench.simulator.protocols.fasthotstuff.FastHotStuffScenarioExecutor;
import byzzbench.simulator.protocols.pbft_java.PbftScenarioExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.Serializable;

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
  private ScenarioExecutor<? extends Serializable> scenarioExecutor =
      new XRPLScenarioExecutor();

  @EventListener(ApplicationReadyEvent.class)
  void onStartup() {
    log.info("Starting the simulator service");
    this.changeScenario("xrpl");
    log.info("Simulator service started");
  }

  public void changeScenario(String id) {
    switch (id) {
    case "fasthotstuff":
      this.scenarioExecutor = new FastHotStuffScenarioExecutor();
      break;
    case "pbft-java":
      this.scenarioExecutor = new PbftScenarioExecutor<>();
      break;
    case "xrpl":
      this.scenarioExecutor = new XRPLScenarioExecutor();
      break;
    default:
      throw new IllegalArgumentException("Unknown scenario id: " + id);
    }
    this.scenarioExecutor.setup();
    this.scenarioExecutor.run();
    // this.scenarioExecutor.reset();
  }
}
