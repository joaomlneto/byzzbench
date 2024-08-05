package byzzbench.simulator.service;

import byzzbench.simulator.transport.Schedule;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SchedulesService {
    private final List<Schedule> schedules = new ArrayList<>();

    public void addSchedule(Schedule schedule) {
        schedules.add(schedule);
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }
}
