package byzzbench.simulator.protocols.Zyzzyva;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.extern.java.Log;

@Log
public class SpeculativeHistory {
    private final LinkedHashMap<Long, Long> speculativeHistory;

    public SpeculativeHistory() {
        this.speculativeHistory = new LinkedHashMap<>();
        this.speculativeHistory.put(0L, 0L);
    }

    /**
     * Add an operation to the speculative history at a specific index
     * @param index - the index to add the operation at
     * @param historyHash - the hash to add
     */
    public void add(long index, long historyHash) {
        this.speculativeHistory.put(index, historyHash);
    }

    /**
     * Remove everything but the last node in the speculative history
     * This is used for when we create a checkpoint because at that point, we don't need everything before the last node.
     */
    public void truncate() {
        if (this.speculativeHistory.isEmpty()) {
            return;
        }
        // remove everything except the last node
        while (this.speculativeHistory.size() > 1) {
            this.speculativeHistory.remove(this.speculativeHistory.keySet().iterator().next());
        }
    }

    /**
     * Remove everything before a specific index in the speculative history
     * @param index - the index to truncate the speculative history to
     */
    public void truncate(long index) {
        if (this.speculativeHistory.isEmpty()) {
            return;
        }
        long i = this.speculativeHistory.sequencedKeySet().getFirst();
        if (index == i) {
            log.warning("Trying to truncate to the same index");
            return;
        }
        while (i < index) {
            this.speculativeHistory.remove(i);
            i = this.speculativeHistory.sequencedKeySet().getFirst();
        }
    }

    /**
     * Get the operation at a specific index
     * @param index - the index to get the operation from
     * @throws IndexOutOfBoundsException - if the index is out of bounds
     */
    public long get(long index) throws IndexOutOfBoundsException {
        if (!this.has(index)) {
            throw new IndexOutOfBoundsException("Index " + index + " not found, currently have " + this.speculativeHistory.keySet());
        }
        return this.speculativeHistory.get(index);
    }

    /**
     * Get the last operation in the speculative history
     * @throws IndexOutOfBoundsException - if the speculative history is empty
     */
    public long getLast() throws IndexOutOfBoundsException {
        if (this.speculativeHistory.isEmpty()) {
            throw new IndexOutOfBoundsException("Speculative history is empty");
        }
        return this.speculativeHistory.lastEntry().getValue();
    }

    public void clear() {
        this.speculativeHistory.clear();
    }

    /**
     * Check if the speculative history has a hash at a specific index
     * @param index - the index to check
     * @return true if the index is in the speculative history, false otherwise
     */
    public boolean has(long index) {
        return this.speculativeHistory.containsKey(index);
    }

    public Map.Entry<Long, Long> getLastEntry() {
        return this.speculativeHistory.lastEntry();
    }

    public void truncateTail(long index) {
        if (this.speculativeHistory.isEmpty()) {
            return;
        }
        long i = this.speculativeHistory.sequencedKeySet().getLast();
        if (index == i) {
            log.warning("Trying to truncate to the same index");
            return;
        }
        while (i > index) {
            this.speculativeHistory.remove(i);
            i = this.speculativeHistory.sequencedKeySet().getLast();
        }
    }

    public long getFirstKey() {
        return this.speculativeHistory.sequencedKeySet().getFirst();
    }

    /**
     * Get the length of the speculative history
     * @return the length of the speculative history
     */
    public int getSize() {
        return this.speculativeHistory.size();
    }
}
