package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.protocols.pbft.Principal;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * A QueryStable message: see Query_stable.h/cc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class QueryStableMessage extends MessagePayload {
    public static final String TYPE = "QueryStable";
    /**
     * Identifier of the replica that generated the message.
     */
    private final String id;

    /**
     * A nonce
     */
    private final int nonce;

    /**
     * Signature
     */
    private final byte[] signature;

    /**
     * Recomputes the authenticator in the message using the most recent keys
     *
     * @param p the principal to use for re-authentication
     */
    public void re_authenticate(Principal p) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Recomputes the authenticator in the message using the most recent keys
     */
    public void re_authenticate() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns the identifier of the replica that generated the message.
     */
    public String id() {
        return this.id;
    }

    /**
     * Fetches the nonce in the message
     *
     * @return the nonce
     */
    public int nonce() {
        return this.nonce;
    }

    /**
     * Verifies if the message is signed by the replica rep().id
     *
     * @return true if the message is signed by the replica rep().id, false otherwise
     */
    public boolean verify() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
