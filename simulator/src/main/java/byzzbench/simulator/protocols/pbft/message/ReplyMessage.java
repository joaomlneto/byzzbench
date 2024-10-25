package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.protocols.pbft.CertifiableMessage;
import byzzbench.simulator.protocols.pbft.Digest;
import byzzbench.simulator.protocols.pbft.Principal;
import byzzbench.simulator.transport.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;
import lombok.extern.java.Log;

/**
 * A "Reply" message to the Client: see Reply.h/cc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
@Log
@AllArgsConstructor
public class ReplyMessage extends MessagePayload implements CertifiableMessage {
    public static final String TYPE = "Reply";
    /**
     * Unique request identifier (rid).
     */
    private final long rid;
    /**
     * Is read only (extra & 1)
     */
    public boolean isReadOnly;
    /**
     * Is signed (extra & 2)
     */
    public boolean isSigned;
    /**
     * Current view (v)
     */
    private long v;
    /**
     * Digest of reply
     */
    private Digest digest;
    /**
     * The ID of the replica sending the reply
     */
    private String replica;
    /**
     * The reply
     */
    private String reply;
    /**
     * MAC
     */
    private byte[] mac;

    /**
     * Creates a new (full) reply message with an empty reply and no authentication.
     * The method store_reply and authenticate should be used to finish message construction.
     *
     * @param view    the view number
     * @param req     the request identifier
     * @param replica the replica that is creating this message
     */
    public ReplyMessage(long view, long req, String replica) {
        this.v = view;
        this.rid = req;
        this.replica = replica;
        this.reply = "";
    }

    /**
     * Creates a new empty Reply message and appends a MAC for principal "p"
     *
     * @param view      the view number
     * @param req       the request identifier
     * @param replica   the replica that is creating this message
     * @param d         the digest
     * @param p         the principal
     * @param tentative whether the reply is tentative
     */
    public ReplyMessage(long view, long req, String replica, Digest d, Principal p, boolean tentative) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Create a new reply message with the same state as this
     *
     * @param r the reply message to copy
     */
    public ReplyMessage(ReplyMessage r) {
        this.v = r.v;
        this.rid = r.rid;
        this.replica = r.replica;
        this.reply = r.reply;
        this.digest = r.digest;
        this.mac = r.mac;
        this.isReadOnly = r.isReadOnly;
        this.isSigned = r.isSigned;
    }

    /**
     * Create a new reply message with the same state as this but with a different replica ID.
     *
     * @param id the new replica ID
     * @return a new reply message with the same state as this but with a different replica ID
     */
    public ReplyMessage copy(String id) {
        return this.withReplica(id);
    }

    /**
     * Store the reply in the message.
     *
     * @param reply the reply
     */
    public void store_reply(String reply) {
        this.reply = reply;
    }

    /**
     * Terminates the construction of a reply message by
     * setting the length of the reply to "act_len", appending a MAC,
     * and trimming any surplus storage.
     *
     * @param p         the principal
     * @param act_len   the actual length of the reply
     * @param tentative whether the reply is tentative
     */
    public void authenticate(Principal p, int act_len, boolean tentative) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Recomputes the authenticator in the message using the most recent key
     *
     * @param p the principal to use for re-authentication
     */
    public void re_authenticate(Principal p) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * If this is tentative, converts this into an identical committed message authenticated for
     * principal "p". otherwise, it does nothing.
     *
     * @param p the principal to authenticate the message
     */
    public void commit(Principal p) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns the view number of the message
     *
     * @return the view number of the message
     */
    public long view() {
        return this.v;
    }

    /**
     * Returns the reply identifier of the message
     *
     * @return the reply identifier of the message
     */
    public long request_id() {
        return this.rid;
    }

    /**
     * Returns the reply identifier of the message
     *
     * @return the reply identifier of the message
     */
    public String id() {
        return this.replica;
    }

    /**
     * Fetches the digest from the message
     *
     * @return the digest from the message
     */
    public Digest digest() {
        return this.digest;
    }

    /**
     * Check whether this is a full reply
     *
     * @return true if this is a full reply, false otherwise
     */
    public boolean full() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Check whether this reply is tentative
     *
     * @return true if this reply is tentative, false otherwise
     */
    public boolean is_tentative() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean match(CertifiableMessage other) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean verify() {
        log.severe("verify(): Not implemented");
        return true;
        //throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean encode() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean decode() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
