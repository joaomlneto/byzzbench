package byzzbench.simulator.protocols.tendermint.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;

@Data
public class ProposalMessage extends MessagePayload {
    private final long height;
    private final long round;
    private final String block;

    @Override
    public String getType() {
        return "PROPOSAL";
    }
}
