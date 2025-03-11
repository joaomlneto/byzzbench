package byzzbench.simulator.domain;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.utils.NonNull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor
public class DeliverMessageAction extends Action {
    /**
     * The unique identifier of the receiving node
     */
    @NonNull
    private String recipientId;

    /**
     * The unique identifier of the client that generated the event.
     */
    @NonNull
    private String senderId;

    /**
     * The time the request was created.
     */
    @NonNull
    private Instant timestamp;

    /**
     * The payload of the message.
     */
    @NonNull
    @Column(columnDefinition = "bytea")
    //@Convert(converter = MessagePayloadJsonConverter.class)
    private MessagePayload payload;

    /**
     * Converts a MessageEvent to a DeliverMessageAction.
     *
     * @param messageEvent The MessageEvent to convert.
     * @return The DeliverMessageAction that represents the delivery of the message.
     */
    public static DeliverMessageAction fromEvent(MessageEvent messageEvent) {
        return DeliverMessageAction.builder()
                .recipientId(messageEvent.getRecipientId())
                .senderId(messageEvent.getSenderId())
                .timestamp(messageEvent.getTimestamp())
                .payload(messageEvent.getPayload())
                .build();
    }

    @Override
    public void accept(Scenario scenario) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
