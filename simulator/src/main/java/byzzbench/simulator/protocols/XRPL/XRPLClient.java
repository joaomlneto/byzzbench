package byzzbench.simulator.protocols.XRPL;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.nodes.Client;
import byzzbench.simulator.nodes.ClientReply;
import byzzbench.simulator.transport.DefaultClientRequestPayload;

public class XRPLClient extends Client {
    public XRPLClient(Scenario scenario, String id) {
        super(scenario, id);
    }

    @Override
    public boolean isRequestCompleted(ClientReply message) {
        return true;
    }

    @Override
    public void initialize() {
        this.getScenario().getTransport().sendMessage(
                this,
                // FIXME: Request IDs are not implemented
                new DefaultClientRequestPayload(0, "tx"),
                "D"
        );
        //this.getScenario().getTransport().sendClientRequest(this.getId(), "tx", "D");
    }
}
