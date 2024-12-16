package byzzbench.simulator.protocols.event_driven_hotstuff.messages;

import lombok.Getter;

@Getter
public class AskMessage extends AbstractMessage {
    private String nodeHash;

    public AskMessage(long viewNumber, String nodeHash) {
        super(MessageType.ASK, viewNumber);
        this.nodeHash = nodeHash;
    }
}
