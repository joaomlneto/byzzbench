package byzzbench.simulator.service;

import byzzbench.simulator.ScenarioExecutor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for creating scenarios. Scans the classpath for all classes that
 * implement the ScenarioExecutor interface and registers them by id.
 */
@Service
public class ScenarioFactoryService {
    Map<String, ScenarioExecutor> scenarios = new HashMap<>();

    public ScenarioFactoryService(List<? extends ScenarioExecutor> scenarioExecutors) {
        for (ScenarioExecutor scenarioExecutor : scenarioExecutors) {
            if (scenarios.containsKey(scenarioExecutor.getId())) {
                throw new IllegalArgumentException("Duplicate scenario id: " + scenarioExecutor.getId());
            }
            scenarios.put(scenarioExecutor.getId(), scenarioExecutor);
        }
    }

    /**
     * Get a scenario by id
     * @param id the id of the scenario
     * @return the scenario
     * @throws IllegalArgumentException if the scenario id is unknown
     */
    public ScenarioExecutor getScenario(String id) {
        ScenarioExecutor scenario = scenarios.get(id);
        if (scenario == null) {
            throw new IllegalArgumentException("Unknown scenario id: " + id);
        }
        return scenario;
    }

    /**
     * Get the ids of all registered scenarios
     * @return the ids of all registered scenarios
     */
    public List<String> getScenarioIds() {
        return List.copyOf(scenarios.keySet());
    }
}
