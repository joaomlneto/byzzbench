package byzzbench.simulator.protocols.hbft.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class PrepareMessage extends IPhaseMessage {
    private final long viewNumber;
    private final long sequenceNumber;
    private final byte[] digest;
    // Might not be needed
    //private final String message;
    private RequestMessage request;

    @Override
    public String getType() {
        return "PREPARE";
    }

    @Override
    public long getRound() {
        return this.sequenceNumber * 3 - 2;
    }
}
