package byzzbench.simulator.protocols.Zyzzyva.message;

import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.messages.MessageWithRound;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.With;

@Data
@EqualsAndHashCode(callSuper = true)
@With
public class IHateThePrimaryMessage extends MessagePayload implements MessageWithRound {
    private final long viewNumber;


    @Override
    public long getRound() {
        return viewNumber;
    }

    @Override
    public String getType() {
        return "I_HATE_THE_PRIMARY";
    }

}
