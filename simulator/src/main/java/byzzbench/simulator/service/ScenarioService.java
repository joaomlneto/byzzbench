package byzzbench.simulator.service;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioFactory;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for creating scenarios. Scans the classpath for all classes that
 * implement the ScenarioExecutor interface and registers them by id.
 */
@Service
@Getter
@Log
public class ScenarioService {
    private final MessageMutatorService messageMutatorService;

    /**
     * Map of scenario id to scenario factory bean
     */
    private final SortedMap<String, ScenarioFactory> scenarioFactories = new TreeMap<>();

    /**
     * List of scenarios that have been created by this service
     */
    @Getter
    private final List<Scenario> scenarios = Collections.synchronizedList(new ArrayList<>());

    public ScenarioService(List<? extends ScenarioFactory> scenarioExecutors, MessageMutatorService messageMutatorService) {
        this.messageMutatorService = messageMutatorService;
        for (ScenarioFactory scenarioExecutor : scenarioExecutors) {
            if (scenarioFactories.containsKey(scenarioExecutor.getId())) {
                throw new IllegalArgumentException("Duplicate scenario id: " + scenarioExecutor.getId());
            }
            scenarioFactories.put(scenarioExecutor.getId(), scenarioExecutor);
        }
    }

    /**
     * Get a scenario by id
     *
     * @param id the id of the scenario
     * @return the scenario
     * @throws IllegalArgumentException if the scenario id is unknown
     */
    public Scenario generateScenario(String id, JsonNode parameters) {
        ScenarioFactory scenario = scenarioFactories.get(id);
        if (scenario == null) {
            log.severe("Unknown scenario: " + id);
            log.severe("Available scenarios:");
            for (String scenarioId : scenarioFactories.keySet()) {
                log.severe("- " + scenarioId);
            }
            throw new IllegalArgumentException("Unknown scenario id: " + id);
        }
        Scenario s = scenario.createScenario(messageMutatorService, parameters);
        this.scenarios.add(s);
        return s;
    }

    /**
     * Get the ids of all registered scenarios
     *
     * @return the ids of all registered scenarios
     */
    public List<String> getScenarioIds() {
        return List.copyOf(scenarioFactories.keySet());
    }
}
