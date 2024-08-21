package byzzbench.simulator.faults;

import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

/**
 * Abstract class for mutating {@link MessageEvent}.
 * This class is used as a base to introduce arbitrary faults in the simulation.
 */
@Getter
@ToString
@RequiredArgsConstructor
public abstract class MessageMutationFault implements Fault {
    @NonNull
    private final String id;
    @NonNull
    private final String name;
    @NonNull
    private final Collection<Class<? extends Serializable>> inputClasses;

    /**
     * Checks if this mutator can be applied to the target class
     * @param targetClass the target class to check
     * @return True if the mutator can be applied to the target class, false otherwise
     */
    public boolean canMutateClass(Class<? extends Serializable> targetClass) {
        return inputClasses.stream().anyMatch(clazz -> clazz.isAssignableFrom(targetClass));
    }

    public boolean canMutate(Serializable target) {
        return inputClasses.stream().anyMatch(clazz -> clazz.isAssignableFrom(target.getClass()));
    }

    /**
     * Checks if the event can be mutated by this mutator
     * @param ctx The context of the fault
     * @return True if the message can be mutated by this mutator, false otherwise
     */
    @Override
    public final boolean test(FaultInput ctx) {
        Optional<Event> event = ctx.getEvent();
        return event.isPresent()
                && event.get() instanceof MessageEvent messageEvent
                && canMutate(messageEvent.getPayload());
    }

}
