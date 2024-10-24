package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.protocols.pbft.CertifiableMessage;
import byzzbench.simulator.protocols.pbft.Digest;
import byzzbench.simulator.protocols.pbft.PbftMessagePayloadWithSequenceNumber;
import byzzbench.simulator.protocols.pbft.ReqQueue;
import byzzbench.simulator.transport.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * A PrePrepare message from the Primary to the Replicas: see PrePrepare.h/cc.
 * <p>
 * In the pre-prepare phase, the primary assigns a sequence number n to the request,
 * multicasts a pre-prepare message with m piggybacked to all the backups, and
 * appends the message to its log. The message has the form:
 * <<PRE-PREPARE, v, n, m>, d>, where:
 * - v is the view number,
 * - n is the sequence number,
 * - m is the client's request,
 * - d is the digest of m.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
@Log
@AllArgsConstructor
public class PrePrepareMessage extends MessagePayload implements PbftMessagePayloadWithSequenceNumber, CertifiableMessage {
    public static final String TYPE = "PrePrepare";
    /**
     * Maximum number of big reqs in pre-prepares.
     * Original defined as sizeof(BR_map)*8
     */
    public static final int big_req_max = 8;

    /**
     * Maximum number of requests in a pre-prepare message.
     */
    public static final int max_reqs = 100;

    /**
     * The view number
     */
    private final long v;

    /**
     * The sequence number
     */
    private final long seqno;
    /**
     * The set of requests
     */
    private final List<RequestMessage> requests = new ArrayList<>();
    /**
     * Digests of big requests
     */
    private final List<Digest> big_reqs = new ArrayList<>();
    /**
     * A variable length signature
     */
    private String signature;

    // below are the variable-length fields from the original PrePrepareMessage
    /**
     * Non-deterministic choices
     */
    private String non_det = "";
    /**
     * Digest of request set concatenated with big reqs and non-deterministic choices
     */
    private Digest digest;

    public PrePrepareMessage(long v, long s, ReqQueue reqs) {
        this.v = v;
        this.seqno = s;

        // Fill in the request portion with as many requests as possible and compute digest
        List<Digest> big_req_ds = new ArrayList<>(big_req_max);

        Iterator<ReqQueue.PNode> it = reqs.queueIterator();

        while (it.hasNext()) {
            ReqQueue.PNode pn = it.next();

            // Small request?
            if (!pn.r.isBigRequest()) {
                // Small requests are inlined in the pre-prepare message
                if (this.requests.size() < max_reqs) {
                    this.requests.add(pn.r);
                    reqs.remove();
                } else {
                    break;
                }
            } else {
                // Big requests are sent offline and their digests are sent with pre-prepare message
                if (big_req_ds.size() < big_req_max && this.requests.size() < max_reqs) {
                    big_req_ds.add(pn.r.digest());

                    // Add request to replica's big reqs table
                    Optional<RequestMessage> req = reqs.remove();
                    assert req.isPresent();
                    reqs.getReplica().big_reqs().add_pre_prepare(req.get(), s, v);
                } else {
                    break;
                }
            }
        }

        // Put big requests after regular ones
        for (int i = 0; i < big_req_ds.size(); i++) {
            throw new UnsupportedOperationException("Big requests logic not implemented");
        }

        if (!this.requests.isEmpty() || !this.big_reqs.isEmpty()) {
            // Fill in the non-deterministic choices portion.
            // This is a placeholder for now.
            log.log(java.util.logging.Level.WARNING, "Non-deterministic choices not implemented");
            this.non_det = "";
        } else {
            this.non_det = "";
        }

        // Finalize digest of requests and non-det-choices.
        log.log(java.util.logging.Level.WARNING, "Digest not implemented");

        // Compute authenticator and update size.
        log.log(java.util.logging.Level.WARNING, "Authenticator not implemented");
        this.signature = reqs.getReplica().id();
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Returns the number of big requests in this pre-prepare message.
     *
     * @return the number of big requests in this
     */
    public int num_big_reqs() {
        //throw new UnsupportedOperationException("Not implemented");
        return this.big_reqs.size();
    }


    @Override
    public long seqno() {
        return this.seqno;
    }

    @Override
    public boolean match(CertifiableMessage other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String id() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean verify() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean full() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean encode() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean decode() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long view() {
        return this.v;
    }

    /**
     * Returns a buffer that can be filled with non-deterministic choices
     *
     * @param len the length of the buffer
     * @return a buffer that can be filled with non-deterministic choices
     */
    public byte[] choices(int len) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
