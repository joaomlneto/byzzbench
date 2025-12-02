package byzzbench.simulator.protocols.pbft_java.message;

import byzzbench.simulator.nodes.ClientReply;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.io.Serializable;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ReplyMessage extends MessagePayload implements ClientReply {
    private final Serializable requestId;

    private final long viewNumber;

    @EqualsAndHashCode.Exclude
    private final Instant timestamp;

    private final String clientId;

    @EqualsAndHashCode.Exclude
    private final String replicaId;

    private final Serializable result;

    @Override
    public String getType() {
        return "REPLY";
    }

    @Override
    public Serializable getReply() {
        return result;
    }
}
