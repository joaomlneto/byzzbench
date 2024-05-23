package bftbench.runner.protocols.fasthotstuff.message;

import bftbench.runner.state.PartialOrderLogEntry;
import bftbench.runner.transport.MessagePayload;
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
