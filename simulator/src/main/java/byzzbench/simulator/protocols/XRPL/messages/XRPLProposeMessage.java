package byzzbench.simulator.protocols.XRPL.messages;

import byzzbench.simulator.protocols.XRPL.XRPLProposal;
import byzzbench.simulator.transport.MessagePayload;

public class XRPLProposeMessage implements MessagePayload {
    private XRPLProposal prop;
    private String senderId;

    public XRPLProposeMessage(XRPLProposal prop_) {
        this.prop = prop_;
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
