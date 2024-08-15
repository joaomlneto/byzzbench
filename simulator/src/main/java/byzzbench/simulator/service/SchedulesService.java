package byzzbench.simulator.service;

import byzzbench.simulator.schedule.Schedule;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SchedulesService {
    private final List<Schedule> schedules = Collections.synchronizedList(new ArrayList<>());

    /**
     * Add a new empty schedule
     * @return the new schedule
     */
    public Schedule addSchedule() {
        return this.addSchedule(new Schedule());
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

    public List<Schedule> getSchedules() {
        return schedules;
    }
}
