package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.protocols.Zyzzyva.CommitCertificate;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.io.Serializable;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ViewChangeMessage extends MessagePayload {
    private final long futureViewNumber;
    private final long stableCheckpoint;
    /// TODO: initialize the message log with checkpoint messages
    private final List<CheckpointMessage> checkpoints;
    // CC is either the most recent commit certificate for a request since the last view change,
    // f + 1 view-confirm messages if no commit certificate is available,
    // or a new-view message if neither of the previous options are available.
    private final CommitCertificate commitCertificate;
    // O is i’s ordered request history since the commit certificate indicated by CC
    private final SortedMap<Long, OrderedRequestMessageWrapper> orderedRequestHistory;
    private final String replicaId;

    @Override
    public String getType() {
        return "VIEW_CHANGE";
    }
}