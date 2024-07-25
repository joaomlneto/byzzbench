package byzzbench.simulator.protocols.XRPL.messages;

import byzzbench.simulator.protocols.XRPL.XRPLProposal;
import byzzbench.simulator.transport.MessagePayload;
import lombok.With;
import byzzbench.simulator.transport.SignableMessage;


/*
 * Node Proposal message sent in the establish phase.
 * A node sends this message when it is trying to propose
 * a ledger for the given sequence number, a node may update
 * its proposal via other propose messages with subsequent 
 * sequence numbers.
 */

@With
public class XRPLProposeMessage extends SignableMessage {
    private XRPLProposal prop;
    private String senderId;

    public XRPLProposeMessage(XRPLProposal prop_, String id) {
        this.prop = prop_;
        this.senderId = id;
    }

    @Override
    public String getType() {
        return "PROPOSE";
    }

    public String getSenderId() {
        return this.senderId;
    }    

    public XRPLProposal getProposal() {
        return this.prop;
    }

}
