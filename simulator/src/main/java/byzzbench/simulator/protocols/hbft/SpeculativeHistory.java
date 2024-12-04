package byzzbench.simulator.protocols.hbft;

import java.io.Serializable;
import java.util.Objects;
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

    public void addAll(SortedMap<Long, RequestMessage> history) {
        for (Long key : history.keySet()) {
            this.history.put(key, history.get(key));
        }
    }

    public void addEntry(long sequenceNumber, RequestMessage request) {
        history.put(sequenceNumber, request);
    }

    public void fillMissing(SpeculativeHistory requests) {
        for (Long key : requests.getHistory().keySet()) {
            // If the history is different thats a problem and should throw exception
            if (requests.getHistory().get(key) != null && this.history.containsKey(key) && !this.history.get(key).equals(requests.getHistory().get(key))) {
                System.out.println("HISTORY IS DIFFERENT: SAFETY VIOLATION");
            } else if (requests.getHistory().get(key) != null) {
                this.history.put(key, requests.getHistory().get(key));
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

    public void removeNullEntries() {
        // Iterate over the map and remove entries with null values
        this.history.values().removeIf(Objects::isNull);
    }

    /* 
     * Get the history before a given sequence number (including)
     */
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
        if (this.history.isEmpty()) {
            return 0;
        }
        return this.history.lastKey();
    }

    public boolean isEmpty() {
        return this.history.isEmpty();
    }

}