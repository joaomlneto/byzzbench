package byzzbench.simulator.protocols.XRPL;

import java.util.ArrayList;
import java.util.List;

public class XRPLConsensusResult {
  private List<String> txList;
  private XRPLProposal proposal;
  private List<DisputedTx> disputeds;
  private long roundTime;

  public XRPLConsensusResult() {
    this.txList = new ArrayList<>();
    this.proposal = null;
    this.disputeds = new ArrayList<>();
    this.roundTime = 0;
  }

  public XRPLConsensusResult(List<String> txList) {
    this.txList = new ArrayList<>();
    for (String tx : txList) {
      this.txList.add(tx);
    }
    this.proposal = null;
    this.disputeds = null;
    this.roundTime = 0;
    this.disputeds = new ArrayList<>();
  }

  public List<String> getTxList() { return txList; }
  public XRPLProposal getProposal() { return proposal; }
  public long getRoundTime() { return roundTime; }

  public List<DisputedTx> getDisputedTxs() { return this.disputeds; }

  public void reset() {
    this.txList = new ArrayList<>();
    this.proposal = null;
    this.disputeds = new ArrayList<>();
    this.roundTime = 0;
  }

  public boolean containsTx(String tx) { return this.txList.contains(tx); }

  public void addDisputed(DisputedTx dt) { this.disputeds.add(dt); }

  public void addTx(String tx) { this.txList.add(tx); }

  public void removeTx(String tx) { this.txList.remove(tx); }

  public void setProposal(XRPLProposal prop) { this.proposal = prop; }

  public void resetDisputes() { this.disputeds.clear(); }
}
