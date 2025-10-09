package byzzbench.simulator.service;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioFactory;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.repository.ScheduleRepository;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for creating scenarios. Scans the classpath for all classes that
 * implement the ScenarioExecutor interface and registers them by id.
 */
@Service
@Getter
@Log
public class ScenarioService {
    private final MessageMutatorService messageMutatorService;
    private final ScheduleRepository scheduleRepository;
    private final AtomicLong scenarioSequenceNumber = new AtomicLong(1);

    /**
     * Map of scenario id to scenario factory bean
     */
    private final SortedMap<String, ScenarioFactory> scenarioFactories = new TreeMap<>();

    /**
     * The scenarios that are currently active (i.e. active simulations in memory)
     */
    @Getter
    private final Map<Long, Scenario> scenarios = new HashMap<>();

    public ScenarioService(List<? extends ScenarioFactory> scenarioExecutors, MessageMutatorService messageMutatorService, ScheduleRepository scheduleRepository) {
        this.messageMutatorService = messageMutatorService;
        this.scheduleRepository = scheduleRepository;
        for (ScenarioFactory scenarioExecutor : scenarioExecutors) {
            if (scenarioFactories.containsKey(scenarioExecutor.getId())) {
                throw new IllegalArgumentException("Duplicate scenario id: " + scenarioExecutor.getId());
            }
            scenarioFactories.put(scenarioExecutor.getId(), scenarioExecutor);
        }
    }

    /**
     * Generate a scenario by the ID of the scenario factory and the parameters
     *
     * @param schedule the schedule that describes the scenario to generate
     * @return the unique ID of the scenario
     * @throws IllegalArgumentException if the scenario id is unknown
     */
    public long generateScenario(Schedule schedule) {
        ScenarioParameters parameters = schedule.getParameters();
        ScenarioFactory scenario = scenarioFactories.get(parameters.getScenarioFactoryId());
        if (scenario == null) {
            log.severe("Unknown scenario: " + parameters.getScenarioFactoryId());
            log.severe("Available scenarios:");
            for (String scenarioId : scenarioFactories.keySet()) {
                log.severe("- " + scenarioId);
            }
            throw new IllegalArgumentException("Unknown scenario id: " + parameters.getScenarioFactoryId());
        }
        Scenario s = scenario.createScenario(schedule);
        this.scenarios.put(schedule.getScheduleId(), s);
        return schedule.getScheduleId();
    }

    /**
     * Remove a scenario by id
     *
     * @param scenarioId the id of the scenario to remove
     */
    public void removeScenario(long scenarioId) {
        System.out.println("Current scenarios: " + this.scenarios.keySet());
        System.out.println("Removing scenario: " + scenarioId);
        this.scenarios.remove(scenarioId);
    }

    /**
     * Get the ids of all registered scenarios
     *
     * @return the ids of all registered scenarios
     */
    public List<String> getScenarioFactoryIds() {
        return List.copyOf(scenarioFactories.keySet());
    }

    /**
     * Retrieves a scenario by its unique identifier from the list of created scenarios.
     *
     * @param scenarioId the unique identifier of the desired scenario
     * @return the scenario that matches the specified identifier
     * @throws NoSuchElementException if no scenario with the specified identifier is found
     */
    public Scenario getScenarioById(long scenarioId) {
        if (!scenarios.containsKey(scenarioId)) {
            throw new NoSuchElementException("No scenario found with id: " + scenarioId);
        }
        return scenarios.get(scenarioId);
    }

}
