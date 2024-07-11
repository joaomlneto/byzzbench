package byzzbench.simulator.faults;

import byzzbench.simulator.transport.MessageEvent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FaultPreconditionNegation implements FaultPrecondition {
    private final FaultPrecondition trigger;

    @Override
    public boolean isSatisfiedBy(MessageEvent message) {
        return !trigger.isSatisfiedBy(message);
    }
}
