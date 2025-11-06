package byzzbench.simulator.domain;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.transport.BaseMessageEvent;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.utils.NonNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.UnaryOperator;

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
     * Name of the field within the payload to modify. Supports dot-notation for nested fields (e.g., "header.nonce").
     */
    @NonNull
    private String fieldName;

    /**
     * Transformation function to apply to the current field value. Not persisted/serialized.
     */
    @Transient
    @JsonIgnore
    private UnaryOperator<MessagePayload> transformFunction;

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
    }
}
