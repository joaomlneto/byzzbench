package byzzbench.simulator.faults;

import byzzbench.simulator.transport.MessageEvent;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class FaultTriggerDisjunction implements FaultTrigger {
    private final List<FaultTrigger> triggers;

    public FaultTriggerDisjunction(FaultTrigger... triggers) {
        this.triggers = List.of(triggers);
    }

    @Override
    public boolean isTriggeredBy(MessageEvent message) {
        return triggers.stream().anyMatch(trigger -> trigger.isTriggeredBy(message));
    }
}
