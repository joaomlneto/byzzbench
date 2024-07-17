package byzzbench.simulator.protocols.XRPL.messages;

import byzzbench.simulator.transport.MessagePayload;

public class XRPLSubmitMessage implements MessagePayload {
    //TODO decide how to represent transactions
    public String getType() {
        return "SUBMIT";
    }

}
