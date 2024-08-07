package byzzbench.simulator.service;

import byzzbench.simulator.transport.Schedule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SchedulesService {
  private final List<Schedule> schedules =
      Collections.synchronizedList(new ArrayList<>());

  public void addSchedule(Schedule schedule) { schedules.add(schedule); }

  public List<Schedule> getSchedules() { return schedules; }
}
