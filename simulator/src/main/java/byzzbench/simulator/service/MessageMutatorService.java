package byzzbench.simulator.service;

import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.MessageMutator;
import byzzbench.simulator.transport.MessageMutatorFactory;
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
    private final Map<String, MessageMutator> mutatorsMap = new HashMap<>();

    /**
     * Map of class to the set of mutators that can mutate it.
     */
    private final Map<Class<? extends Serializable>, Set<MessageMutator>> mutatorsByClass = new HashMap<>();

    public MessageMutatorService(List<? extends MessageMutator> mutators, List<? extends MessageMutatorFactory> mutatorFactories) {
        // add mutators to the map
        for (MessageMutator mutator : mutators) {
            if (this.mutatorsMap.containsKey(mutator.getId())) {
                throw new IllegalArgumentException("Duplicate mutator id: " + mutator.getId());
            }
            this.mutatorsMap.put(mutator.getId(), mutator);
        }

        // add mutators from factories to the map
        for (MessageMutatorFactory mutatorFactory : mutatorFactories) {
            List<MessageMutator> mutatorsList = mutatorFactory.mutators();
            for (MessageMutator mutator : mutatorsList) {
                if (this.mutatorsMap.containsKey(mutator.getId())) {
                    throw new IllegalArgumentException("Duplicate mutator id: " + mutator.getId());
                }
                this.mutatorsMap.put(mutator.getId(), mutator);
            }
        }

        // populate mutatorsByClass
        for (MessageMutator mutator : this.mutatorsMap.values()) {
            for (Class<? extends Serializable> clazz : mutator.getInputClasses()) {
                this.mutatorsByClass.computeIfAbsent(clazz, k -> new HashSet<>()).add(mutator);
            }
        }
    }

    public MessageMutator getMutator(String id) {
        if (!this.mutatorsMap.containsKey(id)) {
            throw new IllegalArgumentException("Unknown mutator id: " + id);
        }
        return this.mutatorsMap.get(id);
    }

    public List<MessageMutator> getMutatorsForClass(Class<? extends Serializable> clazz) {
        return new ArrayList<>(this.mutatorsByClass.getOrDefault(clazz, Collections.emptySet()));
    }

    public List<MessageMutator> getMutatorsForEvent(Event event) {
        // return empty list if it is not a message event
        if (!(event instanceof MessageEvent me)) {
            return Collections.emptyList();
        }

        // get the mutators for the event's message class
        return getMutatorsForClass(me.getPayload().getClass());
    }
}
