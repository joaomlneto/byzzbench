package byzzbench.simulator.protocols.tendermint.mutator;

import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.tendermint.message.ProposalMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ToString
public class ProposalMessageMutatorFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault> mutators() {

        return List.of(
                new MessageMutationFault(
                        "tendermint-proposal-height-inc",
                        "Increment Height Number",
                        List.of(ProposalMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ProposalMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        ProposalMessage mutatedMessage = message.withHeight(message.getHeight() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("tendermint-proposal-height-dec", "Decrement Height Number", List.of(ProposalMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ProposalMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        ProposalMessage mutatedMessage = message.withHeight(message.getHeight() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("tendermint-proposal-sequence-inc", "Increment sequence Number", List.of(ProposalMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ProposalMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        ProposalMessage mutatedMessage = message.withSequence(message.getSequence() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("tendermint-proposal-sequence-dec", "Decrement sequence Number", List.of(ProposalMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ProposalMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        ProposalMessage mutatedMessage = message.withSequence(message.getSequence() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                }
//                new MessageMutationFault(
//                        "tendermint-proposal-height-any",
//                        "Assign Random Height Number",
//                        List.of(ProposalMessage.class)) {
//                    @Override
//                    public void accept(FaultContext serializable) {
//                        Optional<Event> event = serializable.getEvent();
//                        if (event.isEmpty()) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(event.get() instanceof MessageEvent messageEvent)) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(messageEvent.getPayload() instanceof ProposalMessage message)) {
//                            throw invalidMessageTypeException;
//                        }
//                        long randomHeight = random.nextLong(1, 10000); // Adjust range as needed
//                        ProposalMessage mutatedMessage = message.withHeight(randomHeight);
//                        mutatedMessage.sign(message.getSignedBy());
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                },
//                new MessageMutationFault(
//                        "tendermint-proposal-sequence-any",
//                        "Assign Random Sequence Number",
//                        List.of(ProposalMessage.class)) {
//                    @Override
//                    public void accept(FaultContext serializable) {
//                        Optional<Event> event = serializable.getEvent();
//                        if (event.isEmpty()) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(event.get() instanceof MessageEvent messageEvent)) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(messageEvent.getPayload() instanceof ProposalMessage message)) {
//                            throw invalidMessageTypeException;
//                        }
//                        long randomSequence = random.nextLong(0, 100000); // Adjust range as needed
//                        ProposalMessage mutatedMessage = message.withSequence(randomSequence);
//                        mutatedMessage.sign(message.getSignedBy());
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                }
        );
    }
}
