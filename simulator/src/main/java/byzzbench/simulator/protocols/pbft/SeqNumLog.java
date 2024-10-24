package byzzbench.simulator.protocols.pbft;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Log of T ordered by sequence number.
 * Request that T has a method: void clear().
 */
public class SeqNumLog<T extends SeqNumLog.SeqNumLogEntry> implements Serializable {
    private final T[] elems;
    private final int max_size;
    private long head;

    /**
     * Creates a new log that holds "sz" elements and has head equal to "h".
     * The log only maintains elements with sequence number higher than "head"
     * and lower than "tail" = "head"+"max_size"-1.
     *
     * @param sz the size of the log
     * @param h  the head of the log
     */
    public SeqNumLog(int sz, long h, Supplier<T> supplier) {
        elems = (T[]) new SeqNumLog.SeqNumLogEntry[sz];
        for (int i = 0; i < sz; i++) {
            elems[i] = supplier.get();
        }
        head = h;
        max_size = sz;
    }

    /**
     * Creates a new log that holds "sz" elements and has head equal to 1.´
     *
     * @param sz the size of the log
     */
    public SeqNumLog(int sz, Supplier<T> supplier) {
        this(sz, 1, supplier);
    }

    /**
     * Calls clear for all elements in log and sets head to "h"
     *
     * @param h the new head
     */
    public void clear(long h) {
        for (T e : elems) {
            e.clear();
        }
        head = h;
    }

    /**
     * Returns the entry corresponding to "seqno". Requires "within_range(seqno)".
     *
     * @param seqno the sequence number
     * @return the entry corresponding to "seqno"
     */
    public T fetch(long seqno) {
        if (!within_range(seqno)) {
            throw new AssertionError(String.format("Expected seqno %d to be within range [%d, %d)", seqno, head, head + max_size));
        }

        return elems[(int) mod(seqno)];
    }

    /**
     * Truncates the log clearing all elements with sequence number lower than new_head.
     *
     * @param new_head the new head
     */
    public void truncate(long new_head) {
        if (new_head <= head) return;

        long i = head;
        long max = new_head;
        if (new_head - head >= max_size) {
            i = 0;
            max = max_size;
        }

        for (; i < max; i++) {
            elems[(int) mod(i)].clear();
        }

        head = new_head;
    }

    /**
     * Returns true iff "seqno" is within range
     *
     * @param seqno the sequence number
     * @return true if "seqno" is within range, false otherwise
     */
    public boolean within_range(long seqno) {
        return seqno >= head && seqno < head + max_size;
    }

    /**
     * Returns the sequence number of the head of the log.
     *
     * @return the sequence number of the head of the log
     */
    public long head_seqno() {
        return head;
    }

    /**
     * Returns the maximum sequence number that can be stored in the log
     *
     * @return the maximum sequence number that can be stored in the log
     */
    public long max_seqno() {
        return head + max_size - 1;
    }

    /**
     * Compute "s" modulo the size of the log
     *
     * @param s the number to be modded
     * @return the result of the modulo operation
     */
    protected long mod(long s) {
        return s % max_size;
    }

    /**
     * A log entry.
     */
    public interface SeqNumLogEntry extends Serializable {
        void clear();
    }
}
