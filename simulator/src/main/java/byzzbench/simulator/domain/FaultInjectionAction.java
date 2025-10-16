package byzzbench.simulator.domain;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.faults.FaultBehavior;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.service.ApplicationContextProvider;
import byzzbench.simulator.service.FaultsFactoryService;
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
    private long eventId;

    /**
     * The unique identifier of the fault.
     */
    private String faultBehaviorId;

    /**
     * The payload of the request.
     */
    @NonNull
    private MutateMessageEventPayload payload;

    /**
     * Converts a FaultBehavior to a FaultInjectionAction.
     *
     * @param faultBehavior The FaultBehavior to convert.
     * @return The FaultInjectionAction that represents the FaultBehavior.
     */
    public static FaultInjectionAction fromEvent(FaultBehavior faultBehavior) {
        return FaultInjectionAction.builder()
                .faultBehaviorId(faultBehavior.getId())
                .build();
    }

    @Override
    public void accept(Scenario scenario) {
        FaultsFactoryService faultsFactoryService = ApplicationContextProvider.getFaultsFactoryService();
        FaultBehavior behavior = faultsFactoryService.getFaultBehavior(faultBehaviorId);
        behavior.accept(new ScenarioContext(scenario, scenario.getTransport().getEvent(eventId)));
    }
}
