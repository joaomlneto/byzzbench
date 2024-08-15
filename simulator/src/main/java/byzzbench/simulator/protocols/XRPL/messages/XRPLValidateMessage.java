package byzzbench.simulator.protocols.XRPL.messages;

import byzzbench.simulator.protocols.XRPL.XRPLLedger;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Getter;
import lombok.Setter;

/*
 * Validate messages are sent in the accept phase. This is
 * when a node decides consensus has been reached for a proposal
 * and announces that it validates the given ledger.
 */

@Getter
public class XRPLValidateMessage extends MessagePayload {
    private final String senderNodeId;
    //private signature sign;
    @Setter
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
