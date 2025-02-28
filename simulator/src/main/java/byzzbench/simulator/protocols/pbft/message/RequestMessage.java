package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.protocols.pbft.Digest;
import byzzbench.simulator.protocols.pbft.PbftReplica;
import byzzbench.simulator.protocols.pbft.Principal;
import byzzbench.simulator.transport.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;
import lombok.extern.java.Log;

import java.io.Serializable;

/**
 * A "Request" message from the Client to the Replicas: see Request.h/cc.
 * <p>
 * In the request phase, a client sends a request to the primary replica.
 * The message has the form: <<REQUEST, cid, rid, c>, d>, where:
 * - cid is the client identifier,
 * - rid is a unique request identifier,
 * - c is the command to be executed,
 * - d is the digest of cid, rid and c.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
@Log
@AllArgsConstructor
public class RequestMessage extends MessagePayload {
    public static final String TYPE = "Request";
    /**
     * Maximum size of not-big requests
     */
    static final int BIG_REQ_THRESH = 0;

    /**
     * ID of the client who sends the request. (cid)
     */
    private final String cid;

    /**
     * ID of replica from which client expects to receive a full reply.
     * If negative, it means all replicas.
     */
    private final String replier;

    /**
     * Unique request identifier (rid).
     */
    private final long rid;
    /**
     * The command to be executed
     */
    private final String command;
    /**
     * Whether the request is read-only.
     */
    private boolean isReadOnly = false;
    /**
     * Whether the request is signed.
     */
    private boolean isSigned = false;
    /**
     * Digest of requestId (rid), clientId (cid) and operation (command).
     */
    private Digest od = null;
    /**
     * Whether the request is classified as a "big request".
     */
    private boolean isBigRequest = false;

    /**
     * Creates a new signed Request message with an empty command and no authentication.
     * The methods store_command and authenticate should be used to finish message construction.
     * "rr" is the identifier of the replica from which the client expects a full reply (if
     * negative, client expects a full reply from all replicas).
     *
     * @param replica The replica that is creating this message.
     * @param r       The unique request identifier.
     * @param rr      The identifier of the replica from which the client expects a full reply.
     */
    public RequestMessage(PbftReplica replica, long r, String rr) {
        this.cid = replica.id();
        this.rid = r;
        this.replier = rr;
        this.command = "";

        throw new UnsupportedOperationException("Not done implementing");
    }

    /**
     * Creates a new signed Request message with an empty command and no authentication.
     * The methods store_command and authenticate should be used to finish message construction.
     * "rr" is the identifier of the replica from which the client expects a full reply (if
     * negative, client expects a full reply from all replicas).
     *
     * @param replica The replica that is creating this message.
     * @param r       The unique request identifier.
     */
    public RequestMessage(PbftReplica replica, long r) {
        this(replica, r, "-1");
    }

    // XXX: workaround constructor for the clients to call, since they're not PbftReplicas.
    public RequestMessage(String cid, long r, String rr, String command) {
        this.cid = cid;
        this.rid = r;
        this.replier = rr;
        this.command = command;
    }

    /**
     * Stores the command in the request message.
     */
    public void store_command(Serializable cmd) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Clones the request message.
     *
     * @return A new request message with the same attributes as the original.
     */
    @Override
    public RequestMessage clone() {
        //return new RequestMessage(clientId, replier, requestId, command, isReadOnly, isSigned, digest);
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Terminates the construction of a request message by
     * setting the length of the command to "act_len", and apending an
     * authenticator. Read-only should be true iff the request is read-only.
     * (i.e., it will not change the service state).
     *
     * @param act_len  The length of the command.
     * @param readOnly Whether the request is read-only.
     */
    public void authenticate(int act_len, boolean readOnly) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Recomputes the authenticator in the request using the most recent keys.
     * If "change" is set to true, it marks the request
     * read-write and changes teh replier to -1. If "p" is not null, may
     * only update "p"'s entry.
     *
     * @param change Whether the request is read-write.
     * @param p      The principal to update.
     */
    public void reAuthenticate(boolean change, Principal p) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Terminates the construction of a request message by setting the length of the
     * command to "act_len", and appending a signature. Read-only requests are never signed.
     *
     * @param act_len The length of the command.
     */
    public void sign(int act_len) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Verifies if the message is authenticated by the client "client_id()" using
     * an authenticator, or a signature
     *
     * @return true if the message is properly authenticated, false otherwise.
     */
    public boolean verify(PbftReplica node) {
        log.severe("RequestMessage::verify(): Not implemented");
        return true;
    }

    public Digest computeDigest() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public RequestRep rep() {
        throw new UnsupportedOperationException("Not implemented");
        //return new RequestRep(digest, replier, clientId, requestId, command, "_AUTHENTICATOR_");
    }

    public Digest digest() {
        return this.od;
    }

    @Data
    public class RequestRep {
        /**
         * Digest of rid, cid, command
         */
        private final Digest od;

        /**
         * ID of replica from which client expects to receive a full reply.
         * If negative, it means all replicas.
         */
        private final String replier;

        /**
         * Unique ID of client who sends the request
         */
        private final String cid;

        /**
         * Unique request identifier
         */
        private final String rid;

        /**
         * The command to be executed
         */
        private final String command;

        /**
         * The authenticator
         */
        private final String authenticator;
    }
}
