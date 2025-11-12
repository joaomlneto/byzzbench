package byzzbench.simulator.domain;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.utils.NonNull;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.java.Log;

import java.time.Instant;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Log
public class DropMessageAction extends Action {
    /**
     * The parameters for the fault
     */
    @NonNull
    private long eventId;

    private String recipientId;
    private String senderId;
    private Instant timestamp;
    private MessagePayload payload;

    /**
     * Converts a MessageEvent to a DeliverMessageAction.
     *
     * @param messageEvent The MessageEvent to convert.
     * @return The DeliverMessageAction that represents the delivery of the message.
     */
    public static DropMessageAction fromEvent(MessageEvent messageEvent) {
        return DropMessageAction.builder()
                .eventId(messageEvent.getEventId())
                .recipientId(messageEvent.getRecipientId())
                .senderId(messageEvent.getSenderId())
                .timestamp(messageEvent.getTimestamp())
                .payload(messageEvent.getPayload())
                .build();
    }

    @Override
    public void accept(Scenario scenario) {
        Event event = scenario.getTransport().getEvent(eventId);

        if (!(event instanceof MessageEvent messageEvent)) {
            log.warning("Event is not a message event");
            return;
        }

        // otherwise, drop the message: the sender and recipient are in different partitions
        if (event.getStatus() == Event.Status.QUEUED) {
            scenario.getTransport().dropEvent(event.getEventId());
        }
    }
}
