package byzzbench.simulator.service;

import byzzbench.simulator.faults.FaultBehavior;
import byzzbench.simulator.faults.MessageMutationFault;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;

/**
 * Registry of fault behaviors.
 */
@Service
public class FaultsFactoryService {
    /**
     * Map of fault behaviors by id.
     */
    SortedMap<String, FaultBehavior> faultBehaviors = new TreeMap<>();

    /**
     * Map of fault behaviors by input class.
     */
    SortedMap<Class<? extends Serializable>, SortedSet<FaultBehavior>> faultBehaviorsByInputClass = new TreeMap<>();

    public FaultsFactoryService(List<? extends FaultBehavior> faultBehaviors) {
        // add fault behaviors to the map
        for (FaultBehavior faultBehavior : faultBehaviors) {
            if (this.faultBehaviors.containsKey(faultBehavior.getId())) {
                throw new IllegalArgumentException("Duplicate fault behavior id: " + faultBehavior.getId());
            }
            this.faultBehaviors.put(faultBehavior.getId(), faultBehavior);
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
