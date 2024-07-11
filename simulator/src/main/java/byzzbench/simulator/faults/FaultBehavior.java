package byzzbench.simulator.faults;

import byzzbench.simulator.transport.MessageEvent;

/**
 * Interface for the behavior of a fault.
 * The behavior of a fault is to mutate a {@link MessageEvent}.
 */
public interface FaultBehavior {
    void mutate(MessageEvent message);
}
