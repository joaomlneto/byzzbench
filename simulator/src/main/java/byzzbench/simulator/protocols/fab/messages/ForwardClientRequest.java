package byzzbench.simulator.protocols.fab.messages;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class ForwardClientRequest extends MessagePayload {
    private final Serializable operation;
    private final String clientId;

    public ForwardClientRequest(Serializable operation, String clientId) {
        this.operation = operation;
        this.clientId = clientId;
    }

    @Override
    public String getType() {
        return "ForwardClientRequest";
    }
}
