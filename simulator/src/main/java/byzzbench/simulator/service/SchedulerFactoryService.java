package byzzbench.simulator.service;

import byzzbench.simulator.scheduler.BaseScheduler;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SchedulerFactoryService {
    Map<String, BaseScheduler> schedulers = new HashMap<>();

    public SchedulerFactoryService(List<? extends BaseScheduler> schedulerList) {
        for (BaseScheduler scheduler : schedulerList) {
            if (schedulers.containsKey(scheduler.getId())) {
                throw new IllegalArgumentException("Duplicate scheduler id: " + scheduler.getId());
            }
            schedulers.put(scheduler.getId(), scheduler);
        }
    }

    /**
     * Get a scheduler by id
     * @param id the id of the scheduler
     * @return the scheduler
     */
    public BaseScheduler getScheduler(String id) {
        BaseScheduler scheduler = schedulers.get(id);
        if (scheduler == null) {
            throw new IllegalArgumentException("Unknown scheduler id: " + id);
        }
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
