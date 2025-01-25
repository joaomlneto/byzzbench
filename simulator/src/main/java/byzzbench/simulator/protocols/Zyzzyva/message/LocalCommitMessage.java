package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class LocalCommitMessage extends MessagePayload implements Comparable<LocalCommitMessage>, MessageWithRound {
    public final long viewNumber;
    public final byte[] digest;
    public final long history;
    public final String replicaId;
    public final String clientId;


    @Override
    public long getRound() {
        return viewNumber;
    }

    @Override
    public int compareTo(LocalCommitMessage o) {
        return CharSequence.compare(this.replicaId, o.replicaId);
    }

    @Override
    public String getType() {
        return "LOCAL_COMMIT_MESSAGE";
    }

}
