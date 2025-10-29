package byzzbench.simulator.service;

import byzzbench.simulator.domain.Campaign;
import byzzbench.simulator.exploration_strategy.ExplorationStrategy;
import byzzbench.simulator.exploration_strategy.ExplorationStrategyParameters;
import byzzbench.simulator.utils.NonNull;
import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
@Log
@DependsOn("applicationContextProvider")
public class ExplorationStrategyService {
    /**
     * Map of exploration strategy classnames to their respective classes
     */
    private final SortedMap<String, Class<ExplorationStrategy>> explorationStrategyClasses = new TreeMap<>();

    /**
     * Available instances of exploration strategies
     */
    SortedMap<String, ExplorationStrategy> explorationStrategies = new TreeMap<>();

    @PostConstruct
    public void onStartup() {
        ClassPathScanningCandidateComponentProvider provider =
                new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(ExplorationStrategy.class));
        Set<BeanDefinition> components = provider.findCandidateComponents("byzzbench");
        components.forEach(bd -> {
            try {
                log.info("Found exploration strategy class: " + bd.getBeanClassName());
                Class<?> cls = Class.forName(bd.getBeanClassName());
                explorationStrategyClasses.put(bd.getBeanClassName(), (Class<ExplorationStrategy>) cls);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        ApplicationContextProvider.getConfig().getExplorationStrategies().forEach(this::createExplorationStrategy);
    }

    /**
     * Create an exploration strategy instance for a campaign
     *
     * @param campaign the campaign to get the exploration strategy for
     * @return the exploration strategy instance
     */
    public ExplorationStrategy createExplorationStrategy(Campaign campaign) {
        // check if this instance id already exists
        if (this.explorationStrategies.containsKey(campaign.getExplorationStrategyInstanceId())) {
            return this.explorationStrategies.get(campaign.getExplorationStrategyInstanceId());
        }

        return this.createExplorationStrategy("campaign-" + campaign.getCampaignId(), campaign.getExplorationStrategyParameters());
    }

    /**
     * Create an exploration strategy instance
     *
     * @param instanceId a unique ID for this instance of the exploration strategy
     * @param parameters the parameters for the exploration strategy
     * @return the new instance
     */
    public ExplorationStrategy createExplorationStrategy(String instanceId, ExplorationStrategyParameters parameters) {
        // check if this instance id already exists
        if (this.explorationStrategies.containsKey(instanceId)) {
            return this.explorationStrategies.get(instanceId);
        }

        try {
            // find the exploration strategy class by its id
            Class<? extends ExplorationStrategy> explorationStrategyClass = this.explorationStrategyClasses.get(parameters.getExplorationStrategyId());

            // if not found, throw an exception
            if (explorationStrategyClass == null) {
                log.severe("Unknown exploration strategy: " + parameters.getExplorationStrategyId());
                log.severe("Available exploration strategies:");
                for (String explorationStrategyClassname : explorationStrategyClasses.keySet()) {
                    log.severe("- " + explorationStrategyClassname);
                }
                throw new IllegalArgumentException("Unknown exploration strategy id: " + parameters.getExplorationStrategyId());
            }

            // instantiate the exploration strategy and initialize it with the schedule parameters
            Class[] constructorParams = new Class[]{};
            Constructor<? extends ExplorationStrategy> cons = explorationStrategyClass.getConstructor(constructorParams);
            ExplorationStrategy explorationStrategy = cons.newInstance();
            explorationStrategy.loadParameters(parameters);

            this.explorationStrategies.put(instanceId, explorationStrategy);
            log.info("Active exploration strategies: " + this.explorationStrategies.keySet());
            return explorationStrategy;
        } catch (Exception e) {
            e.printStackTrace();
            log.severe("Requested exploration strategy: " + parameters.getExplorationStrategyId());
            log.severe("Failed to generate exploration strategy: " + e.getMessage());
            log.severe("Available exploration strategies:");
            for (String explorationStrategyClassname : explorationStrategyClasses.keySet()) {
                log.severe("- " + explorationStrategyClassname);
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * Get an exploration strategy instance by its id
     *
     * @param id the id of the exploration strategy
     * @return the exploration strategy
     */
    public ExplorationStrategy getExplorationStrategy(@NonNull String id) {
        ExplorationStrategy explorationStrategy = explorationStrategies.get(id);
        if (explorationStrategy == null) {
            throw new IllegalArgumentException("Unknown exploration strategy id: " + id);
        }
        return explorationStrategy;
    }

    /**
     * Get the ids of all registered exploration strategies
     *
     * @return the ids of all registered exploration strategies
     */
    public List<String> getSchedulerIds() {
        return List.copyOf(explorationStrategies.keySet());
    }
}
