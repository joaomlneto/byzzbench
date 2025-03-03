package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.protocols.hbft.SpeculativeHistory;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;

public abstract class CheckpointMessage extends MessagePayload implements MessageWithRound{
    public abstract String getReplicaId();

    public abstract long getLastSeqNumber();

    public abstract byte[] getDigest();

    public abstract SpeculativeHistory getHistory();

    /**
     * Get the request of the message.
     *
     * @return The request of the message.
     */
    public long getRound() {
        return getLastSeqNumber();
    }
}
