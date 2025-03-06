package byzzbench.simulator.service;

import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.transport.Action;
import byzzbench.simulator.transport.MessageAction;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;

/**
 * Service for mutating messages.
 */
@Service
public class MessageMutatorService {
    /**
     * Map of mutators by id.
     */
    @Getter
    private final SortedMap<String, MessageMutationFault> mutatorsMap = new TreeMap<>();

    /**
     * Map of class to the set of mutators that can mutate it.
     */
    private final SortedMap<Class<? extends Serializable>, SortedSet<MessageMutationFault>> mutatorsByClass = new TreeMap<>(Comparator.comparing(Class::getName));

    public MessageMutatorService(List<MessageMutationFault> mutators, List<? extends MessageMutatorFactory> mutatorFactories) {
        // add mutators to the map
        for (MessageMutationFault mutator : mutators) {
            if (this.mutatorsMap.containsKey(mutator.getId())) {
                throw new IllegalArgumentException("Duplicate mutator id: " + mutator.getId());
            }
            this.mutatorsMap.put(mutator.getId(), mutator);
        }

        // add mutators from factories to the map
        for (MessageMutatorFactory mutatorFactory : mutatorFactories) {
            List<MessageMutationFault> mutatorsList = mutatorFactory.mutators();
            for (MessageMutationFault mutator : mutatorsList) {
                if (this.mutatorsMap.containsKey(mutator.getId())) {
                    throw new IllegalArgumentException("Duplicate mutator id: " + mutator.getId());
                }
                this.mutatorsMap.put(mutator.getId(), mutator);
            }
        }

        // populate mutatorsByClass
        for (MessageMutationFault mutator : this.mutatorsMap.values()) {
            for (Class<? extends Serializable> clazz : mutator.getInputClasses()) {
                this.mutatorsByClass.computeIfAbsent(clazz, k -> new TreeSet<>()).add(mutator);
            }
        }
    }

    public MessageMutationFault getMutator(String id) {
        if (!this.mutatorsMap.containsKey(id)) {
            throw new IllegalArgumentException("Unknown mutator id: " + id);
        }
        return this.mutatorsMap.get(id);
    }

    public List<MessageMutationFault> getMutatorsForClass(Class<? extends Serializable> clazz) {
        return new ArrayList<>(this.mutatorsByClass.getOrDefault(clazz, Collections.emptySortedSet()));
    }

    public List<MessageMutationFault> getMutatorsForEvent(Action Action) {
        // return empty list if it is not a message event
        if (!(Action instanceof MessageAction me)) {
            return Collections.emptyList();
        }

        // get the mutators for the event's message class
        return getMutatorsForClass(me.getPayload().getClass());
    }
}
