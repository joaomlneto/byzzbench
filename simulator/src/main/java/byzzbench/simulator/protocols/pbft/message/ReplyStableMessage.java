package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.protocols.pbft.Digest;
import byzzbench.simulator.protocols.pbft.PbftReplica;
import byzzbench.simulator.protocols.pbft.Principal;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * A ReplyStable message: see Reply_stable.h/cc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
public class ReplyStableMessage extends MessagePayload {
    public static final String TYPE = "ReplyStable";
    /**
     * Last checkpoint at sending replica
     */
    private final long lc;

    /**
     * Last prepared request at sending replica
     */
    private final long lp;

    /**
     * Identifier of sending replica
     */
    private final String id;

    /**
     * Nonce in query-stable
     */
    private final int nonce;

    /**
     * MAC
     */
    private final Digest mac;

    public ReplyStableMessage(PbftReplica replica, long lc, long lp, int n, Principal p) {
        this.lc = lc;
        this.lp = lp;
        this.id = replica.id();
        this.nonce = n;

        // TODO: p->gen_mac_out()
        this.mac = null;
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
