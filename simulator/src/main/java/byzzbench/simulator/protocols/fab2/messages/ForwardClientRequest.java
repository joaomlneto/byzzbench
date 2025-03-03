package byzzbench.simulator.protocols.fab2.messages;

import byzzbench.simulator.transport.MessagePayload;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.With;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ForwardClientRequest extends MessagePayload {
    @JsonManagedReference
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
