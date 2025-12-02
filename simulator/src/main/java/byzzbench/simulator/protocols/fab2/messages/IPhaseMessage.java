package byzzbench.simulator.protocols.fab2.messages;

import byzzbench.simulator.exploration_strategy.byzzfuzz.MessageWithByzzFuzzRoundInfo;
import byzzbench.simulator.transport.MessagePayload;

public abstract class IPhaseMessage extends MessagePayload implements MessageWithByzzFuzzRoundInfo {
    public abstract String getType();

    public abstract long getViewNumber();

    public abstract long getSequenceNumber();

    public long getRound() {
        return getSequenceNumber();
    }
}
