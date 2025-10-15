package byzzbench.simulator.controller;


import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.service.ScenarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;

@RestController
@RequiredArgsConstructor
public class ScheduleController {
    private final ScenarioService scenarioService;

    /**
     * Get the list of all schedules.
     *
     * @return a list of schedule ids
     */
    @GetMapping("/schedules")
    public List<Long> getSchedules() {
        return StreamSupport.stream(scenarioService.getScheduleRepository().findAll().spliterator(), false)
                .map(Schedule::getScheduleId)
                .toList();
    }

    /**
     * Get a schedule by id.
     *
     * @param scheduleId the id of the schedule
     * @return the schedule
     */
    @GetMapping("/schedules/{scheduleId}")
    public Schedule getSchedule(@PathVariable Long scheduleId) {
        try {
            return scenarioService.getScheduleById(scheduleId);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found");
        }
    }

    /**
     * Materialize the scenario for a given schedule.
     *
     * @param scheduleId the id of the schedule
     * @return the ID of the materialized scenario
     */
    @PostMapping("/schedules/{scheduleId}/materialize")
    public String materializeSchedule(@PathVariable Long scheduleId) {
        try {
            Schedule schedule = scenarioService.getScheduleById(scheduleId);
            schedule.materializeScenario();
            return schedule.getScenario().getDescription();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found");
        }
    }
}
