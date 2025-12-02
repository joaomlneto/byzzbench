package byzzbench.simulator.protocols.hbft.message;

import byzzbench.simulator.exploration_strategy.byzzfuzz.MessageWithByzzFuzzRoundInfo;
import byzzbench.simulator.protocols.hbft.SpeculativeHistory;
import byzzbench.simulator.transport.MessagePayload;

public abstract class CheckpointMessage extends MessagePayload implements MessageWithByzzFuzzRoundInfo {
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
