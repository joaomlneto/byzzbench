package byzzbench.simulator.protocols.fab2.messages;

import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;

public abstract class IPhaseMessage extends MessagePayload implements MessageWithRound {
    public abstract String getType();
    public abstract long getViewNumber();
    public abstract long getSequenceNumber();
    public long getRound() { return getSequenceNumber(); }
}
