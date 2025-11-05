package byzzbench.simulator.service;

import byzzbench.simulator.config.FaultBehaviorConfig;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.FaultBehavior;
import byzzbench.simulator.faults.FaultFactory;
import byzzbench.simulator.faults.FaultPredicate;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Registry of fault behaviors.
 */
@Service
@Log
@DependsOn("applicationContextProvider")
public class FaultsFactoryService {
    /**
     * Map of fault behavior classnames to their respective classes
     */
    private final SortedMap<String, Class<FaultBehavior>> faultBehaviorClasses = new TreeMap<>();

    /**
     * Map of fault predicates by id.
     */
    SortedMap<String, FaultPredicate> faultPredicates = new TreeMap<>();

    /**
     * Map of fault behaviors by id.
     */
    SortedMap<String, FaultBehavior> faultBehaviors = new TreeMap<>();

    /**
     * Map of faults by id
     */
    SortedMap<String, Fault> faults = new TreeMap<>();

    /**
     * Map of fault factories by id.
     */
    SortedMap<String, FaultFactory> faultFactories = new TreeMap<>();

    /**
     * Map of fault behaviors by input class.
     */
    SortedMap<Class<? extends Serializable>, SortedSet<FaultBehavior>> faultBehaviorsByInputClass = new TreeMap<>();

    public FaultsFactoryService(
            List<? extends FaultPredicate> faultPredicates,
            List<? extends FaultBehavior> faultBehaviors,
            List<? extends Fault> faults,
            List<? extends FaultFactory> faultFactories) {
        // add fault behaviors to the map
        for (FaultBehavior faultBehavior : faultBehaviors) {
            if (this.faultBehaviors.containsKey(faultBehavior.getId())) {
                throw new IllegalArgumentException("Duplicate fault behavior id: " + faultBehavior.getId());
            }
            this.faultBehaviors.put(faultBehavior.getId(), faultBehavior);
        }

        // add fault predicates to the map
        for (FaultPredicate faultPredicate : faultPredicates) {
            if (this.faultPredicates.containsKey(faultPredicate.getId())) {
                throw new IllegalArgumentException("Duplicate fault predicate id: " + faultPredicate.getId());
            }
            this.faultPredicates.put(faultPredicate.getId(), faultPredicate);
        }

        // add faults to the map
        for (Fault fault : faults) {
            if (this.faults.containsKey(fault.getId())) {
                throw new IllegalArgumentException("Duplicate fault id: " + fault.getId());
            }
            this.faults.put(fault.getId(), fault);
        }

        // add fault factories to the map
        for (FaultFactory faultFactory : faultFactories) {
            if (this.faultFactories.containsKey(faultFactory.getId())) {
                throw new IllegalArgumentException("Duplicate fault factory id: " + faultFactory.getId());
            }
            this.faultFactories.put(faultFactory.getId(), faultFactory);
        }

        // populate message mutators (fault behaviors specifically applied to events) by input class
        faultBehaviors
                .stream()
                .filter(MessageMutationFault.class::isInstance)
                .map(MessageMutationFault.class::cast)
                .forEach(faultBehavior -> {
                    for (Class<? extends Serializable> inputClass : faultBehavior.getInputClasses()) {
                        faultBehaviorsByInputClass
                                .computeIfAbsent(inputClass, k -> new TreeSet<>())
                                .add(faultBehavior);
                    }
                });

    }

    @PostConstruct
    public void onStartup() {
        ClassPathScanningCandidateComponentProvider provider =
                new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(FaultBehavior.class));
        Set<BeanDefinition> components = provider.findCandidateComponents("byzzbench");
        components.forEach(bd -> {
            try {
                log.info("Found fault behavior class: " + bd.getBeanClassName());
                Class<?> cls = Class.forName(bd.getBeanClassName());
                faultBehaviorClasses.put(bd.getBeanClassName(), (Class<FaultBehavior>) cls);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public FaultBehavior getFaultBehavior(String faultBehaviorId) {
        FaultBehavior faultBehavior = faultBehaviors.get(faultBehaviorId);
        if (faultBehavior == null) {
            throw new IllegalArgumentException("Unknown fault behavior id: " + faultBehaviorId);
        }

        return faultBehavior;
    }

    /**
     * Create a fault behavior from a configuration
     *
     * @param faultBehaviorConfig the configuration to generate a fault behavior
     * @return the fault behavior instance
     */
    public FaultBehavior createFaultBehavior(FaultBehaviorConfig faultBehaviorConfig) {
        try {
            // find the fault behavior class by its id
            Class<? extends FaultBehavior> faultBehaviorClass = this.faultBehaviorClasses.get(faultBehaviorConfig.getFaultBehaviorId());

            // if not found, throw an exception
            if (faultBehaviorClass == null) {
                log.severe("Unknown fault behavior: " + faultBehaviorConfig.getFaultBehaviorId());
                log.severe("Available fault behaviors:");
                for (String explorationStrategyClassname : faultBehaviorClasses.keySet()) {
                    log.severe("- " + explorationStrategyClassname);
                }
                throw new IllegalArgumentException("Unknown fault behavior id: " + faultBehaviorConfig.getFaultBehaviorId());
            }

            // instantiate the exploration strategy and initialize it with the schedule parameters
            Class[] constructorParams = new Class[]{};
            Constructor<? extends FaultBehavior> cons = faultBehaviorClass.getConstructor(constructorParams);
            FaultBehavior faultBehavior = cons.newInstance();

            return faultBehavior;
        } catch (Exception e) {
            log.severe("Requested fault behavior: " + faultBehaviorConfig.getFaultBehaviorId());
            log.severe("Failed to generate fault behavior: " + e.getMessage());
            log.severe("Available fault behaviors:");
            for (String faultBehaviorClassname : faultBehaviorClasses.keySet()) {
                log.severe("- " + faultBehaviorClassname);
            }
            throw new RuntimeException(e);
        }
    }
}
