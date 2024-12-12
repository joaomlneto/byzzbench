package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ViewChangeMessage extends MessagePayload {
    private final long viewNumber;
    // CC is either the most recent commit certificate for a request since the last view change,
    // f + 1 view-confirm messages if no commit certificate is available,
    // or a new-view message if neither of the previous options are available.
    private final Serializable commitCertificate;
    // O is i’s ordered request history since the commit certificate indicated by CC
    private final List<OrderedRequestMessageWrapper> orderedRequestHistory;
    private final String replicaId;

    @Override
    public String getType() {
        return "VIEW_CHANGE";
    }
}