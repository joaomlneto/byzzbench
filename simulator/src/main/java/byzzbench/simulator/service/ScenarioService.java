package byzzbench.simulator.service;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.domain.Action;
import byzzbench.simulator.domain.ScenarioParameters;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.repository.ScheduleRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Service for creating scenarios. Scans the classpath for all classes that
 * implement the ScenarioExecutor interface and registers them by id.
 */
@Service
@Getter
@Log
@RequiredArgsConstructor
public class ScenarioService {
    /**
     * Repository for schedules
     */
    private final ScheduleRepository scheduleRepository;

    /**
     * Map of scenario classnames to their respective classes
     */
    private final SortedMap<String, Class<Scenario>> scenarioClasses = new TreeMap<>();

    /**
     * The schedules that are currently active (with an active simulation in memory)
     */
    @Getter
    private final Map<Long, Schedule> activeSchedules = new HashMap<>();

    @PostConstruct
    public void onStartup() {
        ClassPathScanningCandidateComponentProvider provider =
                new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(Scenario.class));
        Set<BeanDefinition> components = provider.findCandidateComponents("byzzbench");
        components.forEach(bd -> {
            try {
                log.info("Found scenario class: " + bd.getBeanClassName());
                Class<?> cls = Class.forName(bd.getBeanClassName());
                scenarioClasses.put(bd.getBeanClassName(), (Class<Scenario>) cls);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Save all schedules to the database on shutdown
     */
    @PreDestroy
    public void onShutdown() {
        this.saveAllSchedules();
    }

    /**
     * Save all schedules to the database and clear the in-memory cache
     */
    public synchronized void saveAllSchedules() {
        scheduleRepository.saveAll(activeSchedules.values());
        this.activeSchedules.clear();
    }

    /**
     * Save a schedule to the database and remove it from the in-memory cache
     *
     * @param scheduleId the id of the schedule to save
     */
    public synchronized void storeSchedule(long scheduleId) {
        Schedule schedule = this.activeSchedules.get(scheduleId);
        if (schedule != null) {
            scheduleRepository.save(schedule);
            this.activeSchedules.remove(scheduleId);
        }
    }

    /**
     * Generate a scenario from parameters.
     *
     * @param parameters the parameters that describe the scenario to generate
     * @return the generated scenario
     * @throws IllegalArgumentException if the scenario id is unknown
     * @throws IllegalStateException    if the schedule is already materialized
     */
    public synchronized Scenario generateScenario(ScenarioParameters parameters) {
        Schedule schedule = new Schedule(parameters);
        scheduleRepository.save(schedule);
        return this.generateScenario(schedule);
    }

    /**
     * Generate a scenario from a schedule.
     *
     * @param schedule the schedule that describes the scenario to generate
     * @return the generated scenario
     * @throws IllegalArgumentException if the scenario id is unknown
     * @throws IllegalStateException    if the schedule is already materialized
     */
    public synchronized Scenario generateScenario(Schedule schedule) {
        if (schedule.isMaterialized()) {
            throw new IllegalStateException("Schedule is already materialized");
        }

        try {
            ScenarioParameters parameters = schedule.getParameters();
            Class<? extends Scenario> scenarioClass = this.scenarioClasses.get(parameters.getScenarioId());

            if (scenarioClass == null) {
                log.severe("Unknown scenario: " + parameters.getScenarioId());
                log.severe("Available scenarios:");
                for (String scenarioClassName : scenarioClasses.keySet()) {
                    log.severe("- " + scenarioClassName);
                }
                throw new IllegalArgumentException("Unknown scenario id: " + parameters.getScenarioId());
            }

            Class[] constructorParams = new Class[]{Schedule.class};
            Constructor<? extends Scenario> cons = scenarioClass.getConstructor(constructorParams);
            Scenario scenario = cons.newInstance(schedule);
            scenario.setScenarioId(schedule.getScheduleId());
            scenario.loadParameters(schedule.getParameters());

            // apply each action in order
            for (Action action : schedule.getActions()) {
                action.accept(scenario);
            }

            this.activeSchedules.put(schedule.getScheduleId(), schedule);
            return scenario;
        } catch (Exception e) {
            log.severe("Failed to generate scenario: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the ids of all registered scenarios
     *
     * @return the ids of all registered scenarios
     */
    public List<String> getScenarioFactoryIds() {
        return List.copyOf(scenarioClasses.keySet());
    }

    /**
     * Retrieves a scenario by its unique identifier from the list of created scenarios.
     *
     * @param scenarioId the unique identifier of the desired scenario
     * @return the scenario that matches the specified identifier
     * @throws NoSuchElementException if no scenario with the specified identifier is found
     */
    public synchronized Scenario getScenarioById(long scenarioId) {
        Schedule schedule = this.getScheduleById(scenarioId);

        if (!schedule.isMaterialized()) {
            throw new IllegalStateException("Schedule " + scenarioId + " is not materialized");
        }

        return schedule.getScenario();
    }

    /**
     * Get the ids of all currently materialized schedules
     *
     * @return the ids of all materialized schedules
     */
    public synchronized Set<Long> getMaterializedScheduleIds() {
        return this.getActiveSchedules().keySet();
    }

    /**
     * Retrieves a schedule by its unique identifier. If the schedule is not already
     * loaded in memory, it fetches it from the repository and caches it.
     *
     * @param scheduleId the unique identifier of the desired schedule
     * @return the schedule that matches the specified identifier
     * @throws NoSuchElementException if no schedule with the specified identifier is found
     */
    public synchronized Schedule getScheduleById(long scheduleId) {
        // If the schedule is already active, return it
        if (this.activeSchedules.containsKey(scheduleId)) {
            return this.activeSchedules.get(scheduleId);
        }

        // If not, fetch it from the repository.
        // It is guaranteed to be inactive at this point.
        return this.scheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new NoSuchElementException("No schedule found with id: " + scheduleId));
    }

}
