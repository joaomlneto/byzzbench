package byzzbench.simulator.service;

import byzzbench.simulator.config.ByzzBenchConfig;
import byzzbench.simulator.exploration_strategy.ExplorationStrategy;
import byzzbench.simulator.utils.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class ExplorationStrategyService {
    SortedMap<String, ExplorationStrategy> explorationStrategies = new TreeMap<>();

    public ExplorationStrategyService(List<? extends ExplorationStrategy> explorationStrategyList, ByzzBenchConfig config) {
        for (ExplorationStrategy explorationStrategy : explorationStrategyList) {
            if (explorationStrategies.containsKey(explorationStrategy.getId())) {
                throw new IllegalArgumentException("Duplicate exploration_strategy id: " + explorationStrategy.getId());
            }
            explorationStrategies.put(explorationStrategy.getId().toLowerCase(), explorationStrategy);
            explorationStrategy.loadParameters(config.getScheduler());
        }
    }

    /**
     * Get a exploration_strategy by id
     *
     * @param id the id of the exploration_strategy
     * @return the exploration_strategy
     */
    public ExplorationStrategy getExplorationStrategy(@NonNull String id) {
        ExplorationStrategy explorationStrategy = explorationStrategies.get(id.toLowerCase());
        if (explorationStrategy == null) {
            throw new IllegalArgumentException("Unknown exploration_strategy id: " + id);
        }
        return explorationStrategy;
    }

    /**
     * Get the ids of all registered schedulers
     *
     * @return the ids of all registered schedulers
     */
    public List<String> getSchedulerIds() {
        return List.copyOf(explorationStrategies.keySet());
    }
}
