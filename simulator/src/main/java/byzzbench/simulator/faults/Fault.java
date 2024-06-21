package byzzbench.simulator.faults;

import byzzbench.simulator.transport.MessageEvent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Fault {
    private final FaultTrigger trigger;
    private final FaultBehavior behavior;

    public void apply(MessageEvent message) {
        if (trigger.isTriggeredBy(message)) {
            behavior.mutate(message);
        }
    }
}
