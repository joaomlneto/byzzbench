package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;
import org.apache.commons.lang3.tuple.Pair;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ProofOfMisbehaviorMessage extends MessagePayload {
    private final long viewNumber;
    private final Pair<OrderedRequestMessage, OrderedRequestMessage> pom;

    @Override
    public String getType() {
        return "PROOF_OF_MISBEHAVIOR";
    }
}