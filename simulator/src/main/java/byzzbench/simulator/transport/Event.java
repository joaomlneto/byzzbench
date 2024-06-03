package byzzbench.simulator.transport;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
})
public interface Event extends Serializable {
    long getEventId();

    String getSenderId();

    String getRecipientId();

    String toString();

    Instant getCreatedAt();

    Instant getDeliveredAt();

    Status getStatus();

    void setStatus(Status status);

    enum Status {
        QUEUED,
        DELIVERED,
        DROPPED
    }
}
