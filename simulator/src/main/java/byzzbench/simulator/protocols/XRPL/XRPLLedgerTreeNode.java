package byzzbench.simulator.protocols.XRPL;

import java.util.ArrayList;
import java.util.List;

public class XRPLLedgerTreeNode {
  private XRPLLedger ledger;
  private List<XRPLLedgerTreeNode> children;

  public XRPLLedgerTreeNode(XRPLLedger ledger_) {
    this.ledger = ledger_;
    this.children = new ArrayList<>();
  }

  public void addChild(XRPLLedgerTreeNode n) {
    if (this.isNotChildOf(n)) {
      if (n.getLedger().getParentId().equals(this.ledger.getId())) {
        this.children.add(n);
      } else if (!this.children.isEmpty()) {
        for (XRPLLedgerTreeNode node : this.children) {
          node.addChild(n);
        }
      }
    }
  }

  public boolean isNotChildOf(XRPLLedgerTreeNode n) {
    for (XRPLLedgerTreeNode xrplLedgerTreeNode : this.children) {
      if (xrplLedgerTreeNode.getLedger().getId().equals(
              n.getLedger().getId())) {
        return false;
      }
    }
    return true;
  }

  public XRPLLedger getLedger() { return ledger; }

  public List<XRPLLedgerTreeNode> getChildren() { return children; }
}
