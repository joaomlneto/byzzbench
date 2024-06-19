package byzzbench.simulator.protocols.pbft_java.message;

import byzzbench.simulator.transport.MessagePayload;
import lombok.Data;
import lombok.With;

import java.util.Collection;

@Data
@With
public class NewViewMessage implements MessagePayload {
    private final long newViewNumber;
    private final Collection<ViewChangeMessage> viewChangeProofs;
    private final Collection<PrePrepareMessage> preparedProofs;

    @Override
    public String getType() {
        return "NEW-VIEW";
    }
}
