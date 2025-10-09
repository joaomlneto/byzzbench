package byzzbench.simulator.controller;


import byzzbench.simulator.Scenario;
import byzzbench.simulator.domain.Schedule;
import byzzbench.simulator.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.StreamSupport;

@RestController
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleRepository scheduleRepository;

    /**
     * Get the list of all schedules.
     *
     * @return a list of schedule ids
     */
    @GetMapping("/schedules")
    public List<Long> getSchedules() {
        return StreamSupport.stream(scheduleRepository.findAll().spliterator(), false)
                .map(Schedule::getScheduleId)
                .toList();
    }

    /**
     * Get a schedule by id.
     *
     * @param id the id of the schedule
     * @return the schedule
     */
    @GetMapping("/schedules/{id}")
    public Schedule getSchedule(@PathVariable Long id) {
        return scheduleRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));
    }

    /**
     * Materialize the scenario for a given schedule.
     *
     * @param id the id of the schedule
     * @return the ID of the materialized scenario
     */
    @PostMapping("/schedules/{id}/materialize")
    public String materializeSchedule(@PathVariable Long id) {
        Schedule schedule = scheduleRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found"));

        Scenario scenario = schedule.materializeScenario();
        return scenario.getDescription();
    }
}
