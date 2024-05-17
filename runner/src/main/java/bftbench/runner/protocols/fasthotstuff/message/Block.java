package bftbench.runner.protocols.fasthotstuff.message;

import bftbench.runner.transport.MessagePayload;
import lombok.Data;

import java.io.Serializable;

@Data
public class Block implements MessagePayload {
    private final QuorumCertificate qc;
    private final long round;
    private final String author;
    private final Serializable payload;

    @Override
    public String getType() {
        return "BLOCK";
    }
}
