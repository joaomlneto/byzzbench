package byzzbench.simulator.faults;

import byzzbench.simulator.transport.MessageEvent;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class FaultPreconditionConjunction implements FaultPrecondition {
    private final List<FaultPrecondition> triggers;

    public FaultPreconditionConjunction(FaultPrecondition... triggers) {
        this.triggers = List.of(triggers);
    }

    @Override
    public boolean isSatisfiedBy(MessageEvent message) {
        return triggers.stream().allMatch(trigger -> trigger.isSatisfiedBy(message));
    }
}
