package byzzbench.simulator.domain;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.transport.MutateMessageEventPayload;
import byzzbench.simulator.utils.NonNull;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor
public class FaultInjectionAction extends Action {
    /**
     * The unique identifier of the event.
     */
    private String faultId;

    /**
     * The payload of the request.
     */
    @NonNull
    private MutateMessageEventPayload payload;

    @Override
    public void accept(Scenario scenario) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
