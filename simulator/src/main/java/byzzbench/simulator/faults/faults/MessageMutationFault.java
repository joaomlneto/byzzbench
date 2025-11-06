package byzzbench.simulator.faults.faults;

import byzzbench.simulator.domain.Action;
import byzzbench.simulator.domain.CorruptInFlightMessageAction;
import byzzbench.simulator.faults.Fault;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.utils.NonNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Abstract class for mutating {@link MessageEvent}.
 * This class is used as a base to introduce arbitrary faults in the simulation.
 */
@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode
public abstract class MessageMutationFault extends Fault {
    @NonNull
    private final String id;
    @NonNull
    private final String name;
    @NonNull
    private final Collection<Class<? extends Serializable>> inputClasses;

    private String fieldName;
    private UnaryOperator<MessagePayload> transformFunction;

    /**
     * Checks if this mutator can be applied to the target class
     *
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
     *
     * @param ctx The context of the fault
     * @return True if the message can be mutated by this mutator, false otherwise
     */
    @Override
    public final boolean test(ScenarioContext ctx) {
        Optional<Event> event = ctx.getEvent();
        return event.isPresent()
                && event.get() instanceof MessageEvent messageEvent
                && canMutate(messageEvent.getPayload());
    }

    @Override
    public Action toAction(ScenarioContext context) {
        // confirm event exists
        if (context.getEvent().isEmpty()) {
            throw new IllegalStateException("Cannot mutate an empty fault");
        }

        Event event = context.getEvent().get();

        // confirm event is a message
        if (!(event instanceof MessageEvent messageEvent)) {
            throw new IllegalStateException("Cannot mutate an empty fault");
        }

        CorruptInFlightMessageAction action = new CorruptInFlightMessageAction();
        action.setMessageId(event.getEventId());
        action.setFieldName(this.fieldName);
        action.setTransformFunction(this.transformFunction);

        return action;
    }

    public void accept(ScenarioContext context) {
        throw new UnsupportedOperationException("not yet implemented!");
    }
}
