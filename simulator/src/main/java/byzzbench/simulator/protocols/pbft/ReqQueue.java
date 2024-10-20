package byzzbench.simulator.protocols.pbft;

import byzzbench.simulator.protocols.pbft.message.RequestMessage;
import lombok.Getter;

import java.util.*;

/**
 * A bounded queue of requests
 */
public class ReqQueue {
    @Getter
    private final PbftReplica replica;
    private final SortedMap<String, PNode> reqs;
    private final Queue<String> reqs_order = new LinkedList<>();

    /**
     * Creates an empty queue that can hold one request per principal.
     *
     * @param replica the parent replica
     */
    public ReqQueue(PbftReplica replica) {
        this.replica = replica;
        reqs = new TreeMap<>();
    }

    /**
     * Returns the request from client "cid" in the queue. If there is no request from "cid"
     * in the queue, creates a new request and returns it.
     *
     * @param cid the client id
     * @return the request from client "cid" in the queue
     */
    public PNode get(String cid) {
        reqs.computeIfAbsent(cid, k -> new PNode());
        return reqs.get(cid);
    }

    /**
     * If there is space in the queue and there is no request from "r->client_id()" with
     * timestamp greater than or equal to "r"'s in the queue then it appens "r" to the queue,
     * removes any other request from "r->client_id()" from the queue and returns true.
     * Otherwise, returns false.
     *
     * @param r the request to append
     * @return true if the request was appended, false otherwise
     */
    public boolean append(RequestMessage r) {
        String cid = r.getClientId();
        long rid = r.getRequestId();
        PNode cn = this.get(cid);

        // Check if there is a request from the same client in the queue
        if (cn.r != null) {
            // There is a request from cid in reqs.
            if (rid > cn.r.getRequestId()) {
                // The request in reqs is older than r so remove it.
                // rid > cn.r->request_id()
                this.remove(cid, rid);
            } else {
                // The request in reqs is newer than r.
                return false;
            }
        }

        // Append request to queue
        cn.r = r;
        // this.nbytes += r.size(); // FIXME: we are not keeping track of sizes in bytes

        this.reqs_order.add(cid);
        return true;
    }

    /**
     * If there is any element in the queue, removes the first element in the queue and returns it.
     * Otherwise, returns null.
     *
     * @return the first element in the queue or null
     */
    public Optional<RequestMessage> remove() {
        if (this.reqs_order.isEmpty()) {
            return Optional.empty();
        }

        PNode head = this.reqs.get(this.reqs_order.poll());
        RequestMessage ret = head.r;
        if (ret == null) {
            throw new IllegalStateException("Request in queue is null");
        }

        head.r = null;
        return Optional.of(ret);
    }

    /**
     * If there are any requests from client "cid" with timestamp less than or equal to "rid"
     * removes those requests from the queue. Otherwise, does nothing. In either case, it
     * returns true iff the first request in the queue is removed.
     *
     * @param cid the client id
     * @param rid the request id
     * @return true if the first request in the queue is removed
     */
    public boolean remove(String cid, long rid) {
        boolean ret = false;
        PNode cn = this.get(cid);
        if (cn.r != null && cn.r.getRequestId() <= rid) {
            cn.r = null;

            // if the first request in the queue is removed
            assert this.reqs_order.peek() != null;
            if (this.reqs_order.peek().equals(cid)) {
                ret = true;
            }

            // remove cid from the queue
            this.reqs_order.remove(cid);
        }

        return ret;
    }

    /**
     * Removes all the requests from this
     */
    public void clear() {
        this.reqs.clear();
        this.reqs_order.clear();
    }

    /**
     * If there is any element in the queue, returns the first element in the queue.
     *
     * @return the first element in the queue or null if the queue is empty
     */
    public Optional<RequestMessage> first() {
        if (this.reqs_order.isEmpty()) {
            return Optional.empty();
        }

        PNode head = this.reqs.get(this.reqs_order.peek());
        return Optional.ofNullable(head.r);
    }

    /**
     * If there is an element in the queue from client "cid" returns a pointer to the
     * first request in the queue from "cid". Otherwise, returns null.
     *
     * @return the first request in the queue from client "cid" or null
     */
    public Optional<RequestMessage> first_client() {
        Set<String> replicaIds = this.replica.getNodeIds();

        for (String cid : this.reqs_order) {
            if (!replicaIds.contains(cid)) {
                PNode clientNode = this.reqs.get(cid);
                return Optional.ofNullable(clientNode.r);
            }
        }

        return Optional.empty();
    }

    /**
     * Returns trie iff a pre-prepare was sent for a request from "cid" with timestamp greater than
     * or equal to "rid" was sent in view "v". Otherwise, returns false and marks that "rid" is in
     * progress in view "v".
     *
     * @param cid the client id
     * @param rid the request id
     * @param v   the view number
     * @return true if a pre-prepare was sent for a request from "cid" with timestamp greater than
     */
    public boolean in_progress(String cid, long rid, long v) {
        PNode cn = this.get(cid);
        if (rid > cn.out_rid || v > cn.out_v) {
            cn.out_rid = rid;
            cn.out_v = v;
            return false;
        }
        return true;
    }

    /**
     * Get the number of elements in the queue
     *
     * @return the number of elements in the queue
     */
    public int size() {
        return this.reqs_order.size();
    }

    /**
     * Get the number of bytes used by elements in the queue
     *
     * @return the number of bytes used by elements in the queue
     */
    public int num_bytes() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns an iterator over the elements in the queue
     *
     * @return an iterator over the elements in the queue
     */
    public Iterator<PNode> queueIterator() {
        return this.reqs.sequencedValues().iterator();
    }

    /**
     * An entry in the request queue
     */
    public static class PNode {
        public long out_rid;
        public long out_v;
        public RequestMessage r = null;

        public void clear() {
            this.r = null;
            this.out_rid = 0;
            this.out_v = -1;
        }
    }
}
