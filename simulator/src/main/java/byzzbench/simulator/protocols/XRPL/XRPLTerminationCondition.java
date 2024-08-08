package byzzbench.simulator.protocols.XRPL;

import java.util.List;

import byzzbench.simulator.TerminationCondition;
import lombok.extern.java.Log;

@Log
public class XRPLTerminationCondition extends TerminationCondition {
    private final int DESIRED_BLOCK_COUNT = 1;
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
        int needed_count = (int) (networkSize * 0.8);
        int count = 0;
        for (XRPLReplica xrplReplica : replicas) {
            log.info("seq: " + xrplReplica.getValidLedger().getSeq());
            if (xrplReplica.getValidLedger().getSeq() - initialSeq >= DESIRED_BLOCK_COUNT) {
                log.info("satisfies term cond");
                count += 1;
            }
        }    
        log.info("count: " + count);
        return count >= needed_count;
    }

}
