package byzzbench.simulator.faults;

import byzzbench.simulator.transport.MessageEvent;

public interface FaultBehavior {
    void mutate(MessageEvent message);
}
