package byzzbench.simulator.protocols.XRPL.messages;

import byzzbench.simulator.protocols.XRPL.XRPLProposal;
import byzzbench.simulator.transport.SignableMessage;

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
