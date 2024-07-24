package byzzbench.simulator.protocols.XRPL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.With;

@With
public class XRPLProposal {
    private String prevLedgerId;
    private int seq;
    private List<String> txns;
    private String nodeId;
    private long createdTime;

    public XRPLProposal(String prevLedgerId_, int seq_, List<String> txns_, String nodeId_, long createdTime_) {
        this.prevLedgerId = prevLedgerId_;
        this.seq = seq_;
        this.nodeId = nodeId_;
        this.createdTime = createdTime_;
        this.txns = new ArrayList<>();
        for (String tx : txns_) {
            this.txns.add(tx);
        }
    }

    public boolean containsTx(String tx) {
        return this.txns.contains(tx);
    }

    public String getPrevLedgerId() {
        return prevLedgerId;
    }
    public int getSeq() {
        return seq;
    }
    public List<String> getTxns() {
        return txns;
    }
    public String getNodeId() {
        return nodeId;
    }
    public long getCreatedTime() {
        return createdTime;
    }

    public boolean isTxListEqual(List<String> txList) {
        Collections.sort(this.txns);
        Collections.sort(txList);
        return txns.equals(txList);
    }
}
