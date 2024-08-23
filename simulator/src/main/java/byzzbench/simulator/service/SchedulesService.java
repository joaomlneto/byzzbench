package byzzbench.simulator.service;

import byzzbench.simulator.ScenarioExecutor;
import byzzbench.simulator.schedule.Schedule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
@Service
public class SchedulesService {
  private final List<Schedule> schedules =
      Collections.synchronizedList(new ArrayList<>());

  /**
   * Add a new empty schedule
   * @return the new schedule
   */
  public Schedule addSchedule(ScenarioExecutor scenario) {
    Schedule.ScheduleBuilder builder = Schedule.builder();
    Schedule schedule = builder.scenarioId(scenario.getId()).build();
    return this.addSchedule(schedule);
  }

  /**
   * Add an existing schedule to the list of schedules
   * @param schedule the schedule to add
   * @return the schedule
   */
  public Schedule addSchedule(Schedule schedule) {
    schedules.add(schedule);
    return schedule;
  }
}
