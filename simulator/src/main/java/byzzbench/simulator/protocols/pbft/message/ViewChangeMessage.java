package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.protocols.pbft.CertifiableMessage;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.io.Serializable;
import java.util.BitSet;
import java.util.List;

/**
 * A ViewChange message: see View_change.h/cc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ViewChangeMessage extends MessagePayload implements CertifiableMessage {
    public static final String TYPE = "ViewChange";
    /**
     * The replica's new view
     */
    private final long viewNumber;

    /**
     * Sequence number of the last checkpoint known to be stable
     */
    private final long lastStableCheckpoint;

    /**
     * Digests for checkpoints held by the replica in order of increasing
     * sequence number. A null digest means the replica does not have the
     * corresponding checkpoint state.
     */
    private final byte[][] checkpointDigests;

    /**
     * The sending replica's identifier
     */
    private final long replicaId;

    /**
     * Bitmap with bits set for requests that are prepared in requests
     */
    private final BitSet[] missingViewChangeMessages;

    /**
     * Digest of the entire message (except authenticator) with d zeroed.
     */
    private final byte[] digest;

    /**
     * The list of requests
     */
    private final List<RequestInfo> requests;

    /**
     * The authenticator for the message (from principal id?)
     */
    private final byte[] authenticator;

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * RequestInfo describes a request that (1) prepared or (2) for which a pre-prepare/prepare
     * message was sent.
     * <p>
     * In case (1):
     * - the request with digest "d" prepared in view "v" with sequence number "n";
     * - no request prepared with the same sequence number in a later view; and
     * - the last pre-prepare/prepare sent by the replica for this request was for view "lv".
     * <p>
     * In case (2):
     * - a pre-prepare/prepare was sent for a request with digest "d" in view "v" with sequence number "n"; and
     * - no request prepared globally with sequence number "n" in any view "v' <= lv".
     */
    @Data
    public static class RequestInfo implements Serializable {
        private final long lv;
        private final long v;
        private final byte[] digest;
    }
}
