package byzzbench.simulator.protocols.hbft;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpeculativeHistory implements Serializable {
    private Collection<SpeculativeEntry> history = new ArrayList<>();

    public void addEntry(long sequenceNumber, String clientId, Serializable operation, Serializable result) {
        history.add(new SpeculativeEntry(sequenceNumber, clientId, operation, result));
    }

    public SpeculativeHistory getHistory(long sequenceNumber) {
        Collection<SpeculativeEntry> filteredHistory = history.stream().filter(entry -> entry.getSequenceNumber() > sequenceNumber).toList();
        return new SpeculativeHistory(filteredHistory);
    }

    @Data
    public static class SpeculativeEntry implements Serializable {
        private final long sequenceNumber;
        private final String clientId;
        private final Serializable operation;
        private final Serializable result;

        public SpeculativeEntry(long sequenceNumber, String clientId, Serializable operation, Serializable result) {
            this.sequenceNumber = sequenceNumber;
            this.clientId = clientId;
            this.operation = operation;
            this.result = result;
        }
    }
}