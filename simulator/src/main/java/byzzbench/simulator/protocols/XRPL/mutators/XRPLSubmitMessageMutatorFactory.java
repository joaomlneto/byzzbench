package byzzbench.simulator.protocols.XRPL.mutators;

import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.XRPL.messages.XRPLSubmitMessage;
import byzzbench.simulator.transport.Action;
import byzzbench.simulator.transport.MessageAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class XRPLSubmitMessageMutatorFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault("xrpl-submit-change-tx", "Change tx", List.of(XRPLSubmitMessage.class)) {
                    @Override
                    public void accept(ScenarioContext t) {
                        Optional<Action> event = t.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageAction messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof XRPLSubmitMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        XRPLSubmitMessage mutatedMessage = message.withTx(message.getTx() + "01");
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }

                }
        );
    }

}
