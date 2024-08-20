package byzzbench.simulator.protocols.XRPL;

import lombok.Data;
import lombok.With;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@With
public class XRPLProposal implements Serializable {
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

    public boolean isTxListEqual(List<String> txList) {
        Collections.sort(this.txns);
        Collections.sort(txList);
        return txns.equals(txList);
    }
}
