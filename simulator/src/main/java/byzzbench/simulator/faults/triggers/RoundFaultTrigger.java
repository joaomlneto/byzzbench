package byzzbench.simulator.faults.triggers;

import byzzbench.simulator.faults.FaultTrigger;
import byzzbench.simulator.transport.MessageEvent;
import byzzbench.simulator.transport.messages.RoundMessage;

public class RoundFaultTrigger implements FaultTrigger {
    private final long round;

    public RoundFaultTrigger(long round) {
        this.round = round;
    }

    @Override
    public boolean isTriggeredBy(MessageEvent message) {
        if (message instanceof RoundMessage roundMessage) {
            return roundMessage.getRound() == round;
        }
        return false;
    }
}
