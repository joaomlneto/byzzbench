package byzzbench.simulator.protocols.XRPL.mutators;

import byzzbench.simulator.faults.FaultInput;
import byzzbench.simulator.faults.MessageMutationFault;
import byzzbench.simulator.faults.MessageMutatorFactory;
import byzzbench.simulator.protocols.XRPL.messages.XRPLProposeMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class XRPLProposeMessageMutatorFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
            new MessageMutationFault("xrpl-propose-proposal-inc", "Increment Proposal Seq", List.of(XRPLProposeMessage.class)) {
                @Override
                public void accept(FaultInput serializable) {
                    Optional<Event> event = serializable.getEvent();
                    if (event.isEmpty()) {
                        throw invalidMessageTypeException;
                    }
                    if (!(event.get() instanceof MessageEvent messageEvent)) {
                        throw invalidMessageTypeException;
                    }
                    if (!(messageEvent.getPayload() instanceof XRPLProposeMessage message)) {
                        throw invalidMessageTypeException;
                    }
                    XRPLProposeMessage mutatedMessage = message.withProposal(message.getProposal().withSeq(message.getProposal().getSeq() + 1));
                    mutatedMessage.sign(message.getSignedBy());
                    messageEvent.setPayload(mutatedMessage);
                }
            },
            new MessageMutationFault("xrpl-propose-proposal-dec", "Decrement Proposal Seq", List.of(XRPLProposeMessage.class)) {
                @Override
                public void accept(FaultInput serializable) {
                    Optional<Event> event = serializable.getEvent();
                    if (event.isEmpty()) {
                        throw invalidMessageTypeException;
                    }
                    if (!(event.get() instanceof MessageEvent messageEvent)) {
                        throw invalidMessageTypeException;
                    }
                    if (!(messageEvent.getPayload() instanceof XRPLProposeMessage message)) {
                        throw invalidMessageTypeException;
                    }
                    XRPLProposeMessage mutatedMessage = message.withProposal(message.getProposal().withSeq(message.getProposal().getSeq() - 1));
                    mutatedMessage.sign(message.getSignedBy());
                    messageEvent.setPayload(mutatedMessage);
                }
            },
            new MessageMutationFault("xrpl-propose-mutate-tx", "Mutate Tx", List.of(XRPLProposeMessage.class)) {
                @Override
                public void accept(FaultInput serializable) {
                    Optional<Event> event = serializable.getEvent();
                    if (event.isEmpty()) {
                        throw invalidMessageTypeException;
                    }
                    if (!(event.get() instanceof MessageEvent messageEvent)) {
                        throw invalidMessageTypeException;
                    }
                    if (!(messageEvent.getPayload() instanceof XRPLProposeMessage message)) {
                        throw invalidMessageTypeException;
                    }
                    List<String> newTxns = new ArrayList<>();
                    message.getProposal().getTxns().forEach(tx -> {
                        newTxns.add(tx + "01");
                    });
                    XRPLProposeMessage mutatedMessage = message.withProposal(message.getProposal().withTxns(newTxns));
                    mutatedMessage.sign(message.getSignedBy());
                    messageEvent.setPayload(mutatedMessage);
                }
            }
        );
    }

}
