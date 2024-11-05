package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.transport.MessagePayload;

public abstract class CheckpointMessage extends MessagePayload {
    public abstract String getReplicaId();

    public abstract long getLastSeqNumber();

    public abstract byte[] getDigest();
}
