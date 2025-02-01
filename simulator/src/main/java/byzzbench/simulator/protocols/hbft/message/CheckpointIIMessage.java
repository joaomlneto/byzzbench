package byzzbench.simulator.protocols.hbft.message;

import java.util.Map;
import java.util.SortedMap;

import byzzbench.simulator.protocols.hbft.SpeculativeHistory;
import lombok.Data;
import lombok.With;

@Data
@With
public class CheckpointIIMessage extends CheckpointMessage {
    private final long lastSeqNumber;
    // Digest of speculative execution history
    private final byte[] digest;
    private final String replicaId;
    /* 
     * Probably non-standard implementation.
     * As of hbft 4.2 the CHECKPOINT message do not include
     * the speculative history, only the digest.
     * 
     * However, the paper mentions multiple times that,
     * the checkpoint sub-protocol should fix inconsistencies
     * only way for a replica to adjust its history with the checkpoint's
     * history is to have the history itself in the message, so I will include it.
     */
    private final SpeculativeHistory history;

    @Override
    public String getType() {
        return "CHECKPOINT-II";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof CheckpointIIMessage checkpoint) {
            if (checkpoint.getLastSeqNumber() != this.getLastSeqNumber()) {
                return false;
            }

            if (this.history == null && checkpoint.history == null) {
                return true;
            }
            if (this.history == null || checkpoint.history == null) {
                return false; 
            }


            SortedMap<Long, RequestMessage> otherReqs = checkpoint.getHistory().getRequests();
            SortedMap<Long, RequestMessage> thisReqs = this.getHistory().getRequests();
            
            if (thisReqs == null && otherReqs == null) {
                return true;
            }
            if (thisReqs == null || otherReqs == null) {
                return false; // Only one is null
            }
    
            if (thisReqs.size() != otherReqs.size()) {
                return false; // Different sizes
            }
    
            for (Long key : thisReqs.keySet()) {
                if (!otherReqs.containsKey(key) || !otherReqs.get(key).equals(thisReqs.get(key))) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }


    @Override
    public int hashCode() {
        int result = Long.hashCode(getLastSeqNumber());

        if (history != null) {
            SortedMap<Long, RequestMessage> requests = history.getRequests();

            if (requests != null) {
                for (Map.Entry<Long, RequestMessage> entry : requests.entrySet()) {
                    result = 31 * result + Long.hashCode(entry.getKey()); // Hash the key
                    result = 31 * result + (entry.getValue() != null ? entry.getValue().hashCode() : 0); // Hash the value
                }
            }
        }

        return result;
    }
}