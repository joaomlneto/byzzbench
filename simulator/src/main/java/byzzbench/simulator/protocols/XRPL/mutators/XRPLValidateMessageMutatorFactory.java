package byzzbench.simulator.protocols.XRPL.mutators;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.MessageMutationFault;
import byzzbench.simulator.faults.MessageMutatorFactory;
import byzzbench.simulator.protocols.XRPL.XRPLLedger;
import byzzbench.simulator.protocols.XRPL.messages.XRPLValidateMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class XRPLValidateMessageMutatorFactory extends MessageMutatorFactory {

    @Override
    public List<MessageMutationFault> mutators() {
        RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");
        return List.of(
            new MessageMutationFault("change tx", "Change TX", List.of(XRPLValidateMessage.class)) {
                @Override
                public void accept(FaultContext serializable) {
                    Optional<Event> event = serializable.getEvent();
                    if (event.isEmpty()) {
                        throw invalidMessageTypeException;
                    }
                    if (!(event.get() instanceof MessageEvent messageEvent)) {
                        throw invalidMessageTypeException;
                    }
                    if (!(messageEvent.getPayload() instanceof XRPLValidateMessage message)) {
                        throw invalidMessageTypeException;
                    }
                    List<String> newTxns = new ArrayList<>();
                    for (String tx : message.getLedger().getTransactions()) {
                        newTxns.add(tx + "01");
                    }

                    XRPLValidateMessage mutatedMessage = message.withLedger(new XRPLLedger(message.getLedger().getParentId(), message.getLedger().getSeq(), newTxns));
                    mutatedMessage.sign(message.getSignedBy());
                    messageEvent.setPayload(mutatedMessage);
                }
            }
        );
    }

}
