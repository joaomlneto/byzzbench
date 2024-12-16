package byzzbench.simulator.protocols.event_driven_hotstuff.messages;

import byzzbench.simulator.protocols.event_driven_hotstuff.EDHSQuorumCertificate;
import lombok.Getter;

@Getter
public class NewViewMessage extends AbstractMessage {
    EDHSQuorumCertificate justify;

    public NewViewMessage(long viewNumber, EDHSQuorumCertificate justify) {
        super(MessageType.NEW_VIEW, viewNumber);
        this.justify = justify;
    }
}
