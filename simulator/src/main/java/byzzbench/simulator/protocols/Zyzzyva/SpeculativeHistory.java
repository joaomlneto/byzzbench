package byzzbench.simulator.protocols.Zyzzyva;

import java.util.LinkedHashMap;

public class SpeculativeHistory {
    private final LinkedHashMap<Long, Long> speculativeHistory;

    public SpeculativeHistory() {
        this.speculativeHistory = new LinkedHashMap<>();
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
     * @param index
     */
    public void truncate(long index) {
        if (this.speculativeHistory.isEmpty()) {
            return;
        }
        for (long i = this.speculativeHistory.sequencedKeySet().getFirst(); i < index; i++) {
            this.speculativeHistory.remove(i);
        }
    }

    /**
     * Get the operation at a specific index
     * @param index - the index to get the operation from
     * @throws IndexOutOfBoundsException - if the index is out of bounds
     */
    public long get(long index) throws IndexOutOfBoundsException {
        if (!this.has(index)) {
            throw new IndexOutOfBoundsException("Index not found");
        }
        return this.speculativeHistory.get(index);
    }

    /**
     * Get the last operation in the speculative history
     * @throws IndexOutOfBoundsException - if the speculative history is empty
     */
    public long getLast() throws IndexOutOfBoundsException {
        if (!this.has((long) this.speculativeHistory.size() - 1)) {
            throw new IndexOutOfBoundsException("Index " + this.speculativeHistory.size() + " not found");
        }
        return this.speculativeHistory.get((long) this.speculativeHistory.size() - 1);
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

    /**
     * Get the length of the speculative history
     * @return the length of the speculative history
     */
    public int getSize() {
        return this.speculativeHistory.size();
    }
}
