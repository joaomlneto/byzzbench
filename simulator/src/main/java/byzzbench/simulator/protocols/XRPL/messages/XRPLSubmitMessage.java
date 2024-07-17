package byzzbench.simulator.protocols.XRPL.messages;

import byzzbench.simulator.transport.MessagePayload;

public class XRPLSubmitMessage implements MessagePayload {
    private String tx;

    @Override
    public String getType() {
        return "SUBMIT";
    }

    public String getTx() {
        return tx;
    }

}
