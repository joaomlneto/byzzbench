package byzzbench.simulator.protocols.XRPL.messages;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.With;

/*
 * Submit message as used in the gossip protocol.
 * A node broadcasts a submit message to all nodes it
 * is connected once it recieves a transaction from a client.
 */
@Data
@With
public class XRPLSubmitMessage extends MessagePayload {
    private String tx;

    public XRPLSubmitMessage(String tx_) {
        this.tx = tx_;
    }

    @Override
    public String getType() {
        return "SUBMIT";
    }

    public String getTx() {
        return tx;
    }

}
