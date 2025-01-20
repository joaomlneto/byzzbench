package byzzbench.simulator.protocols.event_driven_hotstuff;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Data
public class EDHSScenarioState {
    private boolean livenessViolation;
    private boolean agreementViolation;
    private long maxView;
    private long minView;
    private int commitedNodes;
    private boolean validAssumptions;

    public EDHSScenarioState() {
        livenessViolation = false;
        agreementViolation = false;
        maxView = 0;
        minView = 0;
        commitedNodes = 0;
        validAssumptions = true;
    }

    @Override
    public String toString() {
        return "EDHSScenarioState{" +
                "livenessViolation=" + livenessViolation +
                ", agreementViolation=" + agreementViolation +
                ", maxView=" + maxView +
                ", minView=" + minView +
                ", commitedNodes=" + commitedNodes +
                ", validAssumptions=" + validAssumptions +
                '}';
    }
}
