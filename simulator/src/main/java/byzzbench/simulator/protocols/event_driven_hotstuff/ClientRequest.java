package byzzbench.simulator.protocols.event_driven_hotstuff;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
@EqualsAndHashCode
public class ClientRequest implements Serializable {
    private final String requestId;
    private final String clientId;
    private final Serializable command;

    public ClientRequest(String requestId, String clientId, Serializable command) {
        this.requestId = requestId;
        this.clientId = clientId;
        this.command = command;
    }
}
