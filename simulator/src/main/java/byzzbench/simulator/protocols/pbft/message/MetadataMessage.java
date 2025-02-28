package byzzbench.simulator.protocols.pbft.message;

import byzzbench.simulator.protocols.pbft.CertifiableMessage;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.With;
import lombok.extern.java.Log;

import java.io.Serializable;

/**
 * A Metadata message: see Meta_data.h/cc. This message contains information
 * about a partition and its subpartitions.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Log
@With
public class MetadataMessage extends MessagePayload implements CertifiableMessage {
    public static final String TYPE = "Metadata";
    /**
     * The timestamp of the fetch request (rid)
     */
    private final long requestId;

    /**
     * The last sequence number for which information in this is up-to-date (lu)
     */
    private final long lastUpToDateSeqno;

    /**
     * The sequence number of the last checkpoint that modified the partition (lm)
     */
    private final long lastCheckpointModifiedSeqno;

    /**
     * The level of the partition in the hierarchy (l)
     */
    private final int level;

    /**
     * The index of the partition within the level (i)
     */
    private final int index;

    /**
     * The partition's digest (d)
     */
    private final byte[] digest;

    /**
     * The ID of the sender (id)
     */
    private final String senderId;

    /**
     * The number of sub-partitions included in the message (np)
     */
    private final int numSubPartitions;

    /**
     * The array of subpartition information (parts)
     */
    private final PartInfo[] subPartitions;

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

    @Data
    public static class PartInfo implements Serializable {
        private final int index;
        private final byte[] digest;
    }
}
