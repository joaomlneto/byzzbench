package byzzbench.simulator.protocols.XRPL;

import java.io.Serializable;
import java.util.Set;

public class XRPLProposal implements Serializable {
    private Set<Integer> txSet;

    public XRPLProposal(Set<Integer> txSet) {
        this.txSet = txSet;
    }

    public void addToTxSet(int txid) {
        this.txSet.add(txid);
    }

    
}
