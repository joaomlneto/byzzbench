package byzzbench.simulator.protocols.XRPL.messages;

import byzzbench.simulator.transport.MessagePayload;

/*
 * Dummy client request message containing the
 * transaction to be submitted. Transactions are
 * represented by faulty_safety String values.
 */
public class XRPLTxMessage extends MessagePayload {
    private final String tx;
    private final String senderId;

    public XRPLTxMessage(String tx_, String clientId) {
        this.tx = tx_;
        this.senderId = clientId;
    }

    public String getSenderId() {
        return senderId;
    }

    @Override
    public String getType() {
        return "TX";
    }

    public String getTx() {
        return this.tx;
    }
}
