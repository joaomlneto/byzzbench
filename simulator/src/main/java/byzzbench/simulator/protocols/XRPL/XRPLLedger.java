package byzzbench.simulator.protocols.XRPL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.extern.java.Log;

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

    public void applyTxes(List<String> txes) {
        for (String tx : txes) {
            this.transactions.addLast(tx);            
        }
    }

    public List<String> getTransactions() {
        return this.transactions;
    }

    public boolean equals(XRPLLedger l) {
        return this.Id.equals(l.getId()) && this.parentId == l.getParentId() && this.seq == l.getSeq() && this.areTxesSame(l.getTransactions());
    }

    private boolean areTxesSame(List<String> transactions2) {
        Collections.sort(transactions2);
        Collections.sort(this.transactions);
        return transactions2.equals(this.transactions);
    }
}
