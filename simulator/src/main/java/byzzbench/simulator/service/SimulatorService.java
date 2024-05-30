package byzzbench.simulator.service;

import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.protocols.pbft.PbftScenarioExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Getter
@Service
@RequiredArgsConstructor
public class SimulatorService {
    private final ScenarioExecutor<? extends Serializable> scenarioExecutor = new PbftScenarioExecutor<>();

    @EventListener(ApplicationReadyEvent.class)
    void onStartup() {
        System.out.println("Starting simulator service...");
        this.scenarioExecutor.setup();
        this.scenarioExecutor.run();
    }
}
