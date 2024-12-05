package byzzbench.simulator.protocols.event_driven_hotstuff;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
@EqualsAndHashCode
public class ClientCommand implements Serializable {
    private String clientID;
    private Serializable command;

    public ClientCommand(String clientID, Serializable command) {
        this.clientID = clientID;
        this.command = command;
    }
}
