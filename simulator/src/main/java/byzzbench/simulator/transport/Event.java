package byzzbench.simulator.transport;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

import javax.annotation.processing.Generated;
import java.io.Serializable;
import java.time.Instant;

@Validated
@Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-11-16T16:49:29.361700Z[Europe/Lisbon]")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TimeoutEvent.class, name = "Timeout"),
        @JsonSubTypes.Type(value = MessageEvent.class, name = "Message"),
        @JsonSubTypes.Type(value = ClientRequestEvent.class, name = "ClientRequest"),
        @JsonSubTypes.Type(value = ClientReplyEvent.class, name = "ClientReply"),
        @JsonSubTypes.Type(value = MutateMessageEvent.class, name = "MutateMessage"),
})
public interface Event extends Serializable {
    /**
     * Get the eventId
     * @return a long representing the eventId
     */
    @NonNull
    long getEventId();

    /**
     * Get the senderId
     * @return a String representing the senderId
     */
    String getSenderId();

    /**
     * Get the recipientId
     * @return a String representing the recipientId
     */
    String getRecipientId();

    /**
     * Get a string representation of the event
     * @return a String representing the event
     */
    abstract String toString();

    /**
     * Get the time at which the event was created
     * @return an Instant representing the time at which the event was created
     */
    Instant getCreatedAt();

    /**
     * Get the time at which the event was delivered
     * @return an Instant representing the time at which the event was delivered
     */
    Instant getDeliveredAt();

    Status getStatus();

    void setStatus(Status status);

    enum Status {
        QUEUED,
        DELIVERED,
        DROPPED
    }
}
