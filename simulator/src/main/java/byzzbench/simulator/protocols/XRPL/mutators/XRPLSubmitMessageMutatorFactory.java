package byzzbench.simulator.protocols.XRPL.mutators;

import byzzbench.simulator.faults.FaultInput;
import byzzbench.simulator.faults.MessageMutationFault;
import byzzbench.simulator.faults.MessageMutatorFactory;
import byzzbench.simulator.protocols.XRPL.messages.XRPLSubmitMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Component
public class XRPLSubmitMessageMutatorFactory extends MessageMutatorFactory<Serializable> {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault<Serializable>> mutators() {
        return List.of(
            new MessageMutationFault<>("xrpl-submit-change-tx", "Change tx", List.of(XRPLSubmitMessage.class)) {
                @Override
                public void accept(FaultInput<Serializable> t) {
                    Optional<Event> event = t.getEvent();
                    if (event.isEmpty()) {
                        throw invalidMessageTypeException;
                    }
                    if (!(event.get() instanceof MessageEvent messageEvent)) {
                        throw invalidMessageTypeException;
                    }
                    if (!(messageEvent.getPayload() instanceof XRPLSubmitMessage message)) {
                        throw invalidMessageTypeException;
                    }
                    messageEvent.setPayload(message.withTx(message.getTx() + "01"));
                }

            }
        );
    }

}
