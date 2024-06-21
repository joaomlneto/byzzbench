package byzzbench.simulator.faults;

import byzzbench.simulator.transport.MessageEvent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FaultTriggerNegation implements FaultTrigger {
    private final FaultTrigger trigger;

    @Override
    public boolean isTriggeredBy(MessageEvent message) {
        return !trigger.isTriggeredBy(message);
    }
}
