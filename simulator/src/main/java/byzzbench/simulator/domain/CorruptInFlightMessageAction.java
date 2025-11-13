package byzzbench.simulator.domain;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.service.ApplicationContextProvider;
import byzzbench.simulator.service.MessageMutatorService;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.utils.NonNull;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Action that corrupts a field within the payload of a message currently in-flight.
 * <p>
 * This uses reflection to locate the specified field and applies a transformation function to it.
 */
@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor
public class CorruptInFlightMessageAction extends Action {
    /**
     * The unique identifier of the message event to mutate (i.e., {@link Event#getEventId()}).
     */
    @NonNull
    private long messageId;

    /**
     * The ID of the mutator to be applied
     */
    private String mutatorId;

    @Override
    public void accept(Scenario scenario) {
        MessageMutatorService messageMutatorService = ApplicationContextProvider.getMessageMutatorService();
        MessageMutationFault fault = messageMutatorService.getMutator(mutatorId);

        System.out.println("Applying mutator " + mutatorId + " to message " + messageId);

        Event e = scenario.getTransport().getEvent(this.messageId);
        ScenarioContext context = new ScenarioContext(scenario, e);

        fault.accept(context);
    }

    /*
    private static Field findField(Class<?> clazz, String name) {
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            }
        }
        return null;
    }

    @Override
    public void accept(Scenario scenario) {
        Event event = scenario.getTransport().getEvents().get(this.messageId);
        if (event == null) {
            throw new IllegalArgumentException("No event found with id=" + this.messageId);
        }
        if (!(event instanceof BaseMessageEvent<?> base)) {
            throw new IllegalArgumentException("Event id=" + this.messageId + " is not a MessageEvent");
        }
        Object payloadObj = base.getPayload();
        if (!(payloadObj instanceof MessagePayload)) {
            throw new IllegalStateException("Event payload is not a MessagePayload instance");
        }
        if (transformFunction == null) {
            throw new IllegalStateException("transformFunction must be provided before executing this action");
        }

        String[] parts = this.fieldName.split("\\.");
        Object targetObject = payloadObj;
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            Field f = findField(targetObject.getClass(), part);
            if (f == null) {
                throw new IllegalArgumentException("Field '" + part + "' not found in class " + targetObject.getClass().getName());
            }
            try {
                f.setAccessible(true);
                Object next = f.get(targetObject);
                if (next == null) {
                    throw new IllegalStateException("Intermediate field '" + part + "' is null; cannot traverse to '" + this.fieldName + "'");
                }
                targetObject = next;
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to access field '" + part + "'", e);
            }
        }

        String leafFieldName = parts[parts.length - 1];
        Field leaf = findField(targetObject.getClass(), leafFieldName);
        if (leaf == null) {
            throw new IllegalArgumentException("Field '" + leafFieldName + "' not found in class " + targetObject.getClass().getName());
        }
        try {
            leaf.setAccessible(true);
            MessagePayload currentValue = (MessagePayload) leaf.get(targetObject);
            Object newValue = transformFunction.apply(currentValue);
            // Avoid unnecessary writes if equal
            if (!Objects.equals(currentValue, newValue)) {
                leaf.set(targetObject, newValue);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to mutate field '" + leafFieldName + "'", e);
        }
    }*/
}
