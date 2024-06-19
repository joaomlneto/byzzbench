package byzzbench.simulator.service;

import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.protocols.fasthotstuff.FastHotStuffScenarioExecutor;
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
    private final ScenarioExecutor<? extends Serializable> scenarioExecutor = new FastHotStuffScenarioExecutor();

    @EventListener(ApplicationReadyEvent.class)
    void onStartup() {
        log.info("Starting the simulator service");
        try {
            this.scenarioExecutor.setup();
            this.scenarioExecutor.run();
            this.scenarioExecutor.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("Simulator service started");
    }
}
