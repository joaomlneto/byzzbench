package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.protocols.pbft.Digest;
import byzzbench.simulator.protocols.pbft.PbftReplica;
import byzzbench.simulator.protocols.pbft.Principal;
import byzzbench.simulator.transport.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;

/**
 * A ReplyStable message: see Reply_stable.h/cc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@With
@AllArgsConstructor
public class ReplyStableMessage extends MessagePayload {
    public static final String TYPE = "ReplyStable";
    /**
     * Last checkpoint at sending replica
     */
    private long lc;

    /**
     * Last prepared request at sending replica
     */
    private long lp;

    /**
     * Identifier of sending replica
     */
    private String id;

    /**
     * Nonce in query-stable
     */
    private int nonce;

    /**
     * MAC
     */
    private Digest mac;

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
