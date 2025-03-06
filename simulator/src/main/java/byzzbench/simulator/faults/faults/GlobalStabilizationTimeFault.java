package byzzbench.simulator.faults.faults;

import byzzbench.simulator.faults.BaseFault;
import byzzbench.simulator.faults.behaviors.GlobalStabilizationTimeBehavior;
import byzzbench.simulator.faults.predicates.IsBeforeGST;

/**
 * Triggers the Global Stabilization Time (GST) if not already triggered.
 */
public class GlobalStabilizationTimeFault extends BaseFault {
    public GlobalStabilizationTimeFault() {
        super("GST", new IsBeforeGST(), new GlobalStabilizationTimeBehavior());
    }
}
