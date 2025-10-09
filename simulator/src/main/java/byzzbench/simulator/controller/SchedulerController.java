package byzzbench.simulator.controller;


import byzzbench.simulator.Scenario;
import byzzbench.simulator.scheduler.EventDecision;
import byzzbench.simulator.scheduler.Scheduler;
import byzzbench.simulator.service.ScenarioService;
import byzzbench.simulator.service.SchedulerService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for schedulers
 */
@RestController
@RequiredArgsConstructor
public class SchedulerController {
    private final SchedulerService schedulerService;
    private final ScenarioService scenarioService;


    /**
     * Get the list of schedulers available in the simulator.
     *
     * @return The list of scheduler IDs.
     */
    @GetMapping("/schedulers")
    public List<String> getSchedulers() {
        return schedulerService.getSchedulerIds();
    }

    /**
     * Execute an action in the scenario
     *
     * @param schedulerId The ID of the scheduler to use.
     * @param scenarioId  The ID of the scenario to use.
     * @return The action that was executed.
     */
    @PostMapping("/scheduler/{schedulerId}/{scenarioId}")
    public Optional<EventDecision> executeSchedulerAction(@NonNull @PathVariable("schedulerId") String schedulerId,
                                                          @PathVariable("scenarioId") long scenarioId) {
        Scheduler scheduler = schedulerService.getScheduler(schedulerId);
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        return scheduler.scheduleNext(scenario);
    }

    /**
     * Get the currently selected scheduler
     *
     * @return The list of scheduler IDs.
     */
    @GetMapping("/schedulers/{schedulerId}")
    public Scheduler getScheduler(@NonNull @PathVariable("schedulerId") String schedulerId) {
        return schedulerService.getScheduler(schedulerId);
    }
}
