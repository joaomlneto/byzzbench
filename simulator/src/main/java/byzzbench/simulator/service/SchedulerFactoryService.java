package byzzbench.simulator.service;

import byzzbench.simulator.scheduler.Scheduler;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SchedulerFactoryService {
  Map<String, Scheduler> schedulers = new HashMap<>();

  public SchedulerFactoryService(List<? extends Scheduler> schedulerList) {
    for (Scheduler scheduler : schedulerList) {
      if (schedulers.containsKey(scheduler.getId())) {
        throw new IllegalArgumentException("Duplicate scheduler id: " +
                                           scheduler.getId());
      }
      schedulers.put(scheduler.getId().toLowerCase(), scheduler);
    }
  }

  /**
   * Get a scheduler by id
   * @param id the id of the scheduler
   * @return the scheduler
   */
  public Scheduler getScheduler(String id, JsonNode params) {
    Scheduler scheduler = schedulers.get(id.toLowerCase());
    if (scheduler == null) {
      throw new IllegalArgumentException("Unknown scheduler id: " + id);
    }
    scheduler.loadParameters(params);
    return scheduler;
  }

  /**
   * Get the ids of all registered schedulers
   * @return the ids of all registered schedulers
   */
  public List<String> getSchedulerIds() {
    return List.copyOf(schedulers.keySet());
  }
}
