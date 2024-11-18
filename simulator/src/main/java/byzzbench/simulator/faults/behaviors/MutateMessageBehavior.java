package byzzbench.simulator.faults.behaviors;

import byzzbench.simulator.faults.FaultBehavior;
import byzzbench.simulator.faults.FaultContext;
import lombok.RequiredArgsConstructor;

/**
 * Inject a random message mutation into the message.
 */
@RequiredArgsConstructor
public class MutateMessageBehavior implements FaultBehavior {
    @Override
    public String getId() {
        return "mutatemessage";
    }

    @Override
    public String getName() {
        return "Apply random mutation";
    }

    @Override
    public void accept(FaultContext context) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
