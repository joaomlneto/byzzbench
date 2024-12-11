package byzzbench.simulator.protocols.tendermint.message;

import byzzbench.simulator.transport.DefaultClientRequestPayload;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class PassOnRequest extends MessagePayload {
    private final String replicaId;
    private final DefaultClientRequestPayload request;


    @Override
    public String getType() {
        return "PASS_ON_REQUEST";
    }
}
