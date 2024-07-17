package byzzbench.simulator.protocols.XRPL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class XRPLLedger implements Serializable {
    private String Id;
    private String parentId;
    private int seq;
    List<String> transactions;

    public XRPLLedger(String ID_, String parentID_, int seq_) {
        this.Id = ID_;
        this.parentId = parentID_;
        this.seq = seq_;
        this.transactions = new ArrayList<>();
    }

    public XRPLLedger(XRPLLedger l) {
        this.Id = l.Id;
        this.parentId = l.parentId;
        this.seq = l.seq;
        this.transactions = new ArrayList<>();
        for (String tx : l.transactions) {
            this.transactions.addLast(tx);            
        }
    }

    public String getId() {
        return this.Id;
    }

    public String getParentId() {
        return this.parentId;
    }

    public int getSeq() {
        return this.seq;
    }

    public XRPLLedger applyTxes(List<String> txes) {
        XRPLLedger ret = new XRPLLedger(this);
        for (String tx : txes) {
            ret.transactions.addLast(tx);            
        }
        return ret;
    }

    public List<String> getTransactions() {
        return this.transactions;
    }

    public boolean equals(XRPLLedger l) {
        return this.Id == l.getId() && this.parentId == l.getParentId() && this.seq == l.getSeq() && this.transactions.equals(l.getTransactions());
    }
}
