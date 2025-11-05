package byzzbench.simulator.domain;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.config.FaultBehaviorConfig;
import byzzbench.simulator.faults.FaultBehavior;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.service.ApplicationContextProvider;
import byzzbench.simulator.service.FaultsFactoryService;
import byzzbench.simulator.utils.NonNull;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class FaultInjectionAction extends Action {
    /**
     * The parameters for the fault
     */
    @NonNull
    private FaultBehaviorConfig payload;

    @Override
    public void accept(Scenario scenario) {
        FaultsFactoryService faultsFactoryService = ApplicationContextProvider.getFaultsFactoryService();
        FaultBehavior behavior = faultsFactoryService.createFaultBehavior(payload);
        behavior.accept(new ScenarioContext(scenario, scenario.getTransport().getEvent(Integer.parseInt(payload.getParams().get("eventId")))));
    }
}
