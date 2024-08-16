package byzzbench.simulator.protocols.pbft_java.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class PrePrepareMessage extends IPhaseMessage {
    private long viewNumber;
    private long sequenceNumber;
    private byte[] digest;
    private RequestMessage request;

    @Override
    public String getType() {
        return "PRE-PREPARE";
    }
}
