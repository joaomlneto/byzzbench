package byzzbench.simulator.service;

import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.protocols.fasthotstuff.FastHotStuffScenarioExecutor;
import byzzbench.simulator.protocols.pbft_java.PbftScenarioExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Getter
@Service
@RequiredArgsConstructor
@Log
public class SimulatorService {
    private ScenarioExecutor<? extends Serializable> scenarioExecutor = new PbftScenarioExecutor<>();

    @EventListener(ApplicationReadyEvent.class)
    void onStartup() {
        log.info("Starting the simulator service");
        this.changeScenario("pbft-java");
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
            default:
                throw new IllegalArgumentException("Unknown scenario id: " + id);
        }
        this.scenarioExecutor.setup();
        this.scenarioExecutor.run();
        //this.scenarioExecutor.reset();
    }
}
