package byzzbench.runner.service;

import byzzbench.runner.ScenarioExecutor;
import byzzbench.runner.protocols.pbft.PbftScenarioExecutor;
import io.micronaut.context.annotation.Infrastructure;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Infrastructure
@RequiredArgsConstructor
public class SimulatorService {
    private final ScenarioExecutor<String> scenarioExecutor = new PbftScenarioExecutor<>();

    @EventListener
    void onStartup(ServerStartupEvent event) {
        System.out.println("Starting simulator service...");
        this.scenarioExecutor.setup();
        this.scenarioExecutor.run();
    }
}
