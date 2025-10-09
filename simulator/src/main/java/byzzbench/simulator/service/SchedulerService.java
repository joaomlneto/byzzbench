package byzzbench.simulator.service;

import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.scheduler.Scheduler;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class SchedulerService {
    SortedMap<String, Scheduler> schedulers = new TreeMap<>();

    public SchedulerService(List<? extends Scheduler> schedulerList, ByzzBenchConfig config) {
        for (Scheduler scheduler : schedulerList) {
            if (schedulers.containsKey(scheduler.getId())) {
                throw new IllegalArgumentException("Duplicate scheduler id: " + scheduler.getId());
            }
            schedulers.put(scheduler.getId().toLowerCase(), scheduler);
            scheduler.loadParameters(config.getScheduler());
        }
    }

    @PostConstruct
    public void init() {

    }

    /**
     * Get a scheduler by id
     *
     * @param id the id of the scheduler
     * @return the scheduler
     */
    public Scheduler getScheduler(String id) {
        Scheduler scheduler = schedulers.get(id.toLowerCase());
        if (scheduler == null) {
            throw new IllegalArgumentException("Unknown scheduler id: " + id);
        }
        return scheduler;
    }

    /**
     * Get the ids of all registered schedulers
     *
     * @return the ids of all registered schedulers
     */
    public List<String> getSchedulerIds() {
        return List.copyOf(schedulers.keySet());
    }
}
