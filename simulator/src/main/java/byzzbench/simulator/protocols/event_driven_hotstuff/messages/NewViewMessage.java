package byzzbench.simulator.protocols.event_driven_hotstuff.messages;

import byzzbench.simulator.protocols.event_driven_hotstuff.QuorumCertificate;
import lombok.Getter;

@Getter
public class NewViewMessage extends AbstractMessage {
    QuorumCertificate justify;

    public NewViewMessage(QuorumCertificate justify) {
        super(MessageType.NEW_VIEW);
        this.justify = justify;
    }
}
