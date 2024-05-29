package byzzbench.runner.protocols.fasthotstuff.message;

import byzzbench.runner.state.PartialOrderLogEntry;
import byzzbench.runner.transport.MessagePayload;
import lombok.Data;

import java.io.Serializable;

@Data
public class Block implements MessagePayload, PartialOrderLogEntry<String> {
    private final QuorumCertificate qc;
    private final long round;
    private final String author;
    private final Serializable payload;

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
