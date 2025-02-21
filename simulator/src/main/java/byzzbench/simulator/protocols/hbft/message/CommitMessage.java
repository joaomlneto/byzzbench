package byzzbench.simulator.protocols.hbft.message;

import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.lang3.builder.EqualsExclude;

import byzzbench.simulator.protocols.hbft.SpeculativeHistory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

@Data
//@EqualsAndHashCode(callSuper = true)
@With
public class CommitMessage extends IPhaseMessage {
    private final long viewNumber;
    private final long sequenceNumber;
    private final byte[] digest;
    // Might not be needed
    private final RequestMessage request;
    private final String replicaId;
    // Speculative execution history
    private final SpeculativeHistory speculativeHistory;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof CommitMessage commit) {
            if (commit.getSequenceNumber() != this.getSequenceNumber() || commit.getViewNumber() != this.getViewNumber() ) {
                return false;
            }

            if (!this.request.getOperation().toString().equals(commit.getRequest().getOperation().toString())) {
                return false;
            }

            return true;
        }
        return false;
    }


    @Override
    public int hashCode() {
        int result = Long.hashCode(viewNumber); 
        result = 31 * result + Long.hashCode(sequenceNumber); 
        String operation = request != null ? request.getOperation().toString() : null;
        result = 31 * result + (operation != null ? operation.hashCode() : 0);

        return result;
    }

    @Override
    public String getType() {
        return "COMMIT";
    }

    @Override
    public long getRound() {
        return this.sequenceNumber * 3 - 1;
    }
}
