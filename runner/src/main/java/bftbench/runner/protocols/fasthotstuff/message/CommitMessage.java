package bftbench.runner.protocols.fasthotstuff.message;

import bftbench.runner.protocols.pbft.message.IPhaseMessage;
import bftbench.runner.transport.MessagePayload;
import lombok.Data;
import lombok.With;

@Data
@With
public class CommitMessage implements MessagePayload, IPhaseMessage {
    private final long viewNumber;
    private final long sequenceNumber;
    private final byte[] digest;
    private final String replicaId;

    @Override
    public String getType() {
        return "COMMIT";
    }
}
