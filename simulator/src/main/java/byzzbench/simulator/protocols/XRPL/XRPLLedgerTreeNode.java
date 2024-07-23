package byzzbench.simulator.protocols.XRPL;

import java.util.ArrayList;
import java.util.List;

public class XRPLLedgerTreeNode {
    private String Id;
    private String parentId;
    private List<XRPLLedgerTreeNode> children;

    public XRPLLedgerTreeNode(String id_, String parentId_) {
        this.Id = id_;
        this.parentId = parentId_;
        this.children = new ArrayList<>();
    }

    public void addChild(XRPLLedgerTreeNode n) {
        if (this.isNotChildOf(n)) {
            if (n.getParentId().equals(this.Id)) {
                this.children.add(n);
            } else if (!this.children.isEmpty()){
                for (XRPLLedgerTreeNode node : this.children) {
                    node.addChild(n);
                }
            }
        }
    }

    public boolean isNotChildOf(XRPLLedgerTreeNode n) {
        for (XRPLLedgerTreeNode xrplLedgerTreeNode : this.children) {
            if (xrplLedgerTreeNode.getId().equals(n.getId())) {
                return false;
            }
        }
        return true;
    }

    public String getId() {
        return Id;
    }

    public String getParentId() {
        return parentId;
    }

    public List<XRPLLedgerTreeNode> getChildren() {
        return children;
    }
}
