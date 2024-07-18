package byzzbench.simulator.protocols.XRPL.messages;

import byzzbench.simulator.transport.MessagePayload;

public class XRPLTxMessage implements MessagePayload {
    private String tx;

    public XRPLTxMessage(String tx_) {
        this.tx = tx_;
    }
    @Override
    public String getType() {
        return "TX";
    }

    public String getTx() {
        return this.tx;
    }
}
