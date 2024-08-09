package byzzbench.simulator.service;

import byzzbench.simulator.faults.FaultBehavior;
import byzzbench.simulator.transport.Event;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FaultsFactoryService {
    /**
     * Map of fault behaviors by id.
     */
    Map<String, FaultBehavior<?>> faultBehaviors = new HashMap<>();

    /**
     * Map of fault behaviors by input class.
     */
    Map<Class<? extends Event>, Set<FaultBehavior<?>>> faultBehaviorsByInputClass = new HashMap<>();

    public FaultsFactoryService(List<? extends FaultBehavior<?>> faultBehaviors) {
        // add fault behaviors to the map
        for (FaultBehavior<?> faultBehavior : faultBehaviors) {
            if (this.faultBehaviors.containsKey(faultBehavior.getId())) {
                throw new IllegalArgumentException("Duplicate fault behavior id: " + faultBehavior.getId());
            }
            this.faultBehaviors.put(faultBehavior.getId(), faultBehavior);
        }

        // populate fault behaviors by input class
        for (FaultBehavior<?> faultBehavior : faultBehaviors) {
            for (Class<? extends Event> inputClass : faultBehavior.getInputClasses()) {
                faultBehaviorsByInputClass
                        .computeIfAbsent(inputClass, k -> new HashSet<>())
                        .add(faultBehavior);
            }
        }
    }
}
