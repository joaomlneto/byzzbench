package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class LocalCommitMessage extends MessagePayload implements Comparable<LocalCommitMessage> {
    public final long viewNumber;
    public final byte[] digest;
    public final long history;
    public final String replicaId;
    public final String clientId;

    @Override
    public int compareTo(LocalCommitMessage o) {
        return CharSequence.compare(this.replicaId, o.replicaId);
    }

    @Override
    public String getType() {
        return "LOCAL_COMMIT_MESSAGE";
    }

}
