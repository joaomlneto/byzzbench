package byzzbench.simulator.domain;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.transport.TimeoutEvent;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.time.Instant;

/**
 * Represents the triggering of a timeout event.
 */
@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor
public class TriggerTimeoutAction extends Action {
    /**
     * A human description of the event.
     */
    private String description;

    /**
     * The unique identifier of the node that emitted (and shall receive) it.
     */
    private String nodeId;

    /**
     * The timeout value
     */
    private Duration timeout;

    /**
     * The instant at which the timeout is set to expire in the scenario.
     */
    private Instant expiresAt;

    /**
     * The unique identifier of the event.
     */
    private long timeoutEventId;

    /**
     * Converts a TimeoutEvent to a TriggerTimeoutAction.
     *
     * @param event The TimeoutEvent to convert.
     * @return The TriggerTimeoutAction that represents the triggering of the timeout.
     */
    public static TriggerTimeoutAction fromEvent(TimeoutEvent event) {
        return TriggerTimeoutAction.builder()
                .timeoutEventId(event.getEventId())
                .description(event.getDescription())
                .nodeId(event.getNodeId())
                .timeout(event.getTimeout())
                .expiresAt(event.getExpiresAt())
                .build();
    }

    @Override
    public void accept(Scenario scenario) {
        scenario.getTransport().deliverEvent(this.timeoutEventId, false);
    }
}
