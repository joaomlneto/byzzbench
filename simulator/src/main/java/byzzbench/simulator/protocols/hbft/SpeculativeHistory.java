package byzzbench.simulator.protocols.hbft;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

import byzzbench.simulator.protocols.hbft.message.RequestMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpeculativeHistory implements Serializable {
    private SortedMap<Long, RequestMessage> history = new TreeMap<>();

    public void addEntry(long sequenceNumber, RequestMessage request) {
        history.put(sequenceNumber, request);
    }

    public void rollBack(long sequenceNumber) {
        for (Long key : history.keySet()) {
            if (key > sequenceNumber) {
                this.history.remove(key);
            }
        }
    }

    public SpeculativeHistory getHistory(long sequenceNumber) {
        SortedMap<Long, RequestMessage> filteredHistory = new TreeMap<>();
        for (Long key : this.history.keySet()) {
            if (key > sequenceNumber) {
                filteredHistory.put(key, this.history.get(key));
            }
        }
        return new SpeculativeHistory(filteredHistory);
    }

    public SpeculativeHistory getHistoryBefore(long sequenceNumber) {
        SortedMap<Long, RequestMessage> filteredHistory = new TreeMap<>();
        for (Long key : this.history.keySet()) {
            if (key <= sequenceNumber) {
                filteredHistory.put(key, this.history.get(key));
            }
        }
        return new SpeculativeHistory(filteredHistory);
    }

    public SortedMap<Long, RequestMessage> getRequests(long sequenceNumber) {
        SortedMap<Long, RequestMessage> filteredHistory = new TreeMap<>();
        for (Long key : this.history.keySet()) {
            if (key > sequenceNumber) {
                filteredHistory.put(key, this.history.get(key));
            }
        }
        return filteredHistory;
    }

    public SortedMap<Long, RequestMessage> getRequests() {
        return this.history;
    }

    public long getGreatestSeqNumber() {
        return this.history.lastKey();
    }

    public boolean isEmpty() {
        return history.isEmpty();
    }

}