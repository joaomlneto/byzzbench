package byzzbench.simulator.service;

import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.FaultBehavior;
import byzzbench.simulator.faults.FaultFactory;
import byzzbench.simulator.faults.FaultPredicate;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;

/**
 * Registry of fault behaviors.
 */
@Service
public class FaultsFactoryService {
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
                .filter(faultBehavior -> faultBehavior instanceof MessageMutationFault)
                .map(faultBehavior -> (MessageMutationFault) faultBehavior)
                .forEach(faultBehavior -> {
                    for (Class<? extends Serializable> inputClass : faultBehavior.getInputClasses()) {
                        faultBehaviorsByInputClass
                                .computeIfAbsent(inputClass, k -> new TreeSet<>())
                                .add(faultBehavior);
                    }
                });

    }
}
