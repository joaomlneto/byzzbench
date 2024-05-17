package bftbench.runner.protocols.fasthotstuff.message;

import bftbench.runner.transport.MessagePayload;
import lombok.Data;
import lombok.With;

@Data
@With
public class NewViewMessage implements MessagePayload, GenericVoteMessage {
    private final QuorumCertificate qc;
    private final long round;
    private final String author;
    private final byte[] blockHash;

    @Override
    public String getType() {
        return "NEW-VIEW";
    }
}
