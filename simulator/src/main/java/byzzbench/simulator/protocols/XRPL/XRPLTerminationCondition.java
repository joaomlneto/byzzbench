package byzzbench.simulator.protocols.XRPL;

import java.util.List;

import byzzbench.simulator.TerminationCondition;

public class XRPLTerminationCondition extends TerminationCondition {
    private final int DESIRED_BLOCK_COUNT = 5;

    private List<XRPLReplica> replicas;
    private int initialSeq;
    private int networkSize;
    public XRPLTerminationCondition(List<XRPLReplica> replicas_) {
        this.replicas = replicas_;
        this.initialSeq = 1;
        this.networkSize = replicas_.size();
    }


    @Override
    public boolean shouldTerminate() {
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
