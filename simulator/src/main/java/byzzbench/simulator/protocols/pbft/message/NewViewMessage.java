package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.protocols.pbft.CertifiableMessage;
import byzzbench.simulator.protocols.pbft.VCInfo;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;
import lombok.extern.java.Log;

import java.util.List;

/**
 * A NewView message: see New_view.h/cc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Log
@With
public class NewViewMessage extends MessagePayload implements CertifiableMessage {
    public static final String TYPE = "NewView";
    /**
     * The replica's new view
     */
    private final long viewNumber;

    /**
     * Sequence number of checkpoint chosen for propagation
     */
    private final long minSequenceNumber;

    /**
     * All requests that will be propagated to the next view have sequence number less than maxSequenceNumber
     */
    private final long maxSequenceNumber;

    /**
     * vc_info has information about view-changes selected by primary to form the new view.
     * It has an entry for every replica and is indexed by replica identifier.
     * If a replica's entry has a null digest, its view-change is not part of those selected
     * to form the new-view.
     */
    private final List<VCInfo> vcInfo;

    /**
     * Picked contains identifiers of replicas from whose view-change
     * message a checkpoint value or request was picked for propagation
     * to the new view. It is indexed by sequence number minus minSequenceNumber.
     */
    private final char[] picked;

    /*
      The rationale for including just view-change digests rather than the full messages is that most of the time
      replicas will receive the view-change messages referenced by the new-view message before they receive the
      new-view.
     */

    /**
     * An authenticator
     */
    private final byte[] authenticator;

    @Override
    public String getType() {
        return TYPE;
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
        log.severe("verify(): Not implemented");
        return true;
        //throw new UnsupportedOperationException("Not implemented");
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
}
