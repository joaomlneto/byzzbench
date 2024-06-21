package byzzbench.simulator.faults;

import byzzbench.simulator.transport.MessageEvent;

public interface FaultTrigger {
    boolean isTriggeredBy(MessageEvent message);
}
