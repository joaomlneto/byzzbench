package byzzbench.simulator.protocols.XRPL;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.ScenarioPredicate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.util.List;

@Getter
public class XRPLTerminationCondition implements ScenarioPredicate {
    private final int DESIRED_BLOCK_COUNT = 5;

    @JsonIgnore
    private final List<XRPLReplica> replicas;
    private final int initialSeq;
    private final int networkSize;

    public XRPLTerminationCondition(List<XRPLReplica> replicas_) {
        this.replicas = replicas_;
        this.initialSeq = 1;
        this.networkSize = replicas_.size();
    }


    @Override
    public boolean test(Scenario scenario) {
        return terminateBasedOnNumberOfBlocks();
    }

    @SuppressWarnings("unused")
    private boolean terminateBasedOnNumberOfBlocks() {
        int needed_count = (int) (networkSize * 0.8);
        int count = 0;
        for (XRPLReplica xrplReplica : replicas) {
            if (xrplReplica.getValidLedger().getSeq() - initialSeq >= DESIRED_BLOCK_COUNT) {
                count += 1;
            }
        }
        return count >= needed_count;
    }
}
