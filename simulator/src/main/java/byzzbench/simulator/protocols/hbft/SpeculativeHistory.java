package byzzbench.simulator.protocols.hbft;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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
    private Collection<SpeculativeEntry> history = new ArrayList<>();

    public void addEntry(long sequenceNumber, RequestMessage request) {
        history.add(new SpeculativeEntry(sequenceNumber, request));
    }

    public SpeculativeHistory getHistory(long sequenceNumber) {
        Collection<SpeculativeEntry> filteredHistory = history.stream().filter(entry -> entry.getSequenceNumber() > sequenceNumber).toList();
        return new SpeculativeHistory(filteredHistory);
    }

    public SortedMap<Long, RequestMessage> getRequests(long sequenceNumber) {
        Collection<SpeculativeEntry> filteredHistory = history.stream().filter(entry -> entry.getSequenceNumber() > sequenceNumber).toList();
        SortedMap<Long, RequestMessage> requests = new TreeMap<>();
        for (SpeculativeEntry entry : filteredHistory) {
            requests.put(entry.getSequenceNumber(), entry.getRequest());
        }

        return requests;
    }

    public SortedMap<Long, RequestMessage> getRequests() {
        SortedMap<Long, RequestMessage> requests = new TreeMap<>();
        for (SpeculativeEntry entry : this.history) {
            requests.put(entry.getSequenceNumber(), entry.getRequest());
        }

        return requests;
    }

    public long getGreatesSeqNumber() {
        long maxNumber = 0;
        for (SpeculativeEntry specHistory : history) {
            if (specHistory.getSequenceNumber() > maxNumber) {
                maxNumber = specHistory.getSequenceNumber();
            }
        }

        return maxNumber;
    }

    public boolean isEmpty() {
        return history.isEmpty();
    }

    @Data
    public static class SpeculativeEntry implements Serializable {
        private final long sequenceNumber;
        private final RequestMessage request;
        // Probably not needed
        // private final Serializable result;
        // Maybe also viewNumber, not sure yet
        // private final long viewNumber;

        public SpeculativeEntry(long sequenceNumber, RequestMessage request) {
            this.sequenceNumber = sequenceNumber;
            this.request = request;
            //this.result = result;
        }
    }
}