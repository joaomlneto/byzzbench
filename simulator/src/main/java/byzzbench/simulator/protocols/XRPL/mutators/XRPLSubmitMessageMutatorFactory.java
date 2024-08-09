package byzzbench.simulator.protocols.XRPL.mutators;

import byzzbench.simulator.protocols.XRPL.messages.XRPLSubmitMessage;
import byzzbench.simulator.transport.MessageMutator;
import byzzbench.simulator.transport.MessageMutatorFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
public class XRPLSubmitMessageMutatorFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutator> mutators() {
        return List.of(
            new MessageMutator("xrpl-submit-change-tx", "Change tx", List.of(XRPLSubmitMessage.class)) {

                @Override
                public Serializable apply(Serializable t) {
                    if (t instanceof XRPLSubmitMessage message) {
                        return message.withTx(message.getTx() + "01");
                    }
                    throw  invalidMessageTypeException;
                }

            }
        );
    }

}
