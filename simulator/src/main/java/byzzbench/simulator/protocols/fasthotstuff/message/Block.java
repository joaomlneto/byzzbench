package byzzbench.simulator.protocols.fasthotstuff.message;

import byzzbench.simulator.state.PartialOrderLogEntry;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Block extends MessagePayload implements PartialOrderLogEntry<String> {
    private final GenericQuorumCertificate qc;
    private final long round;
    private final String author;
    private final String payload;

    @Override
    public String getType() {
        return "BLOCK";
    }

    @Override
    public String getParentHash() {
        if (qc == null || qc.getVotes().isEmpty()) {
            return null;
        }
        return qc.getVotes().stream().findAny().get().getBlockHash();
    }
}

