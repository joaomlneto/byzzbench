package byzzbench.simulator.protocols.XRPL.messages;

import byzzbench.simulator.protocols.XRPL.XRPLLedger;
import byzzbench.simulator.transport.SignableMessage;

public class XRPLValidateMessage extends SignableMessage {
    private String senderNodeId;
    //private signature sign;
    private XRPLLedger ledger;

    public XRPLValidateMessage(String nodeId, XRPLLedger l) {
        this.senderNodeId = nodeId;
        this.ledger = l;
    }

    @Override
    public String getType() {
        return "VALIDATE";
    }

    public String getSenderNodeId() {
        return senderNodeId;
    }

    public XRPLLedger getLedger() {
        return ledger;
    }
}
