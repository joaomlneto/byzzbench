package byzzbench.simulator.protocols.XRPL;

import java.io.Serializable;

public class XRPLLedger implements Serializable {
    private String ID;
    private String parentID;
    private int seq;

    public XRPLLedger(String ID_, String parentID_, int seq_) {
        this.ID = ID_;
        this.parentID = parentID_;
        this.seq = seq_;
    }

    public String getID() {
        return this.ID;
    }

    public String getParentID() {
        return this.parentID;
    }

    public int getSeq() {
        return this.seq;
    }
}
