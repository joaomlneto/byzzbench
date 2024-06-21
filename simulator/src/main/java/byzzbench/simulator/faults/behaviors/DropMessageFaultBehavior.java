package byzzbench.simulator.faults.behaviors;

import byzzbench.simulator.faults.FaultBehavior;
import byzzbench.simulator.transport.MessageEvent;

public class DropMessageFaultBehavior implements FaultBehavior {
    @Override
    public void mutate(MessageEvent message) {
        message.setStatus(MessageEvent.Status.DROPPED);
    }
}
