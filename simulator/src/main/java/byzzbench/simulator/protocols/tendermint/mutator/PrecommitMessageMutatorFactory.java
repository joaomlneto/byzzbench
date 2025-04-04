package byzzbench.simulator.protocols.tendermint.mutator;

import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.tendermint.message.PrecommitMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
@ToString
public class PrecommitMessageMutatorFactory extends MessageMutatorFactory {
    private final Random random = new Random();
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault(
                        "tendermint-precommit-height-inc",
                        "Increment Height Number",
                        List.of(PrecommitMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof PrecommitMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        PrecommitMessage mutatedMessage = message.withHeight(message.getHeight() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("tendermint-precommit-height-dec", "Decrement Height Number", List.of(PrecommitMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof PrecommitMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        PrecommitMessage mutatedMessage = message.withHeight(message.getHeight() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("tendermint-precommit-sequence-inc", "Increment sequence Number", List.of(PrecommitMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof PrecommitMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        PrecommitMessage mutatedMessage = message.withSequence(message.getSequence() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("tendermint-precommit-sequence-dec", "Decrement Sequence Number", List.of(PrecommitMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof PrecommitMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        PrecommitMessage mutatedMessage = message.withSequence(message.getSequence() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                }
//                new MessageMutationFault(
//                        "tendermint-precommit-height-any",
//                        "Assign Random Height Number",
//                        List.of(PrecommitMessage.class)) {
//                    @Override
//                    public void accept(FaultContext serializable) {
//                        Optional<Event> event = serializable.getEvent();
//                        if (event.isEmpty()) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(event.get() instanceof MessageEvent messageEvent)) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(messageEvent.getPayload() instanceof PrecommitMessage message)) {
//                            throw invalidMessageTypeException;
//                        }
//                        long randomHeight = random.nextLong(1, 10000); // Adjust range as needed
//                        PrecommitMessage mutatedMessage = message.withHeight(randomHeight);
//                        mutatedMessage.sign(message.getSignedBy());
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                },
//                new MessageMutationFault(
//                        "tendermint-precommit-sequence-any",
//                        "Assign Random Sequence Number",
//                        List.of(PrecommitMessage.class)) {
//                    @Override
//                    public void accept(FaultContext serializable) {
//                        Optional<Event> event = serializable.getEvent();
//                        if (event.isEmpty()) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(event.get() instanceof MessageEvent messageEvent)) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(messageEvent.getPayload() instanceof PrecommitMessage message)) {
//                            throw invalidMessageTypeException;
//                        }
//                        long randomSequence = random.nextLong(0, 100000); // Adjust range as needed
//                        PrecommitMessage mutatedMessage = message.withSequence(randomSequence);
//                        mutatedMessage.sign(message.getSignedBy());
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                }
//                new MessageMutationFault(
//                        "tendermint-precommit-known-violation",
//                        "Known Violation",
//                        List.of(PrecommitMessage.class)) {
//                    @Override
//                    public void accept(FaultContext state) {
//                        Optional<Event> event = state.getEvent();
//                        if (event.isEmpty()) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(event.get() instanceof MessageEvent messageEvent)) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(messageEvent.getPayload() instanceof PrecommitMessage message)) {
//                            throw invalidMessageTypeException;
//                        }
//                        // Check if the recipientId is "B"
//                        if ("B".equals(messageEvent.getRecipientId()) && "A".equals(message.getAuthor())) {
//                            return;
//                        }
//                        if ("A".equals(messageEvent.getRecipientId()) && "B".equals(message.getAuthor())) {
//                            return;
//                        }
//                        state.getScenario().getTransport().dropEvent(messageEvent.getEventId());
//                    }
//                }
        );
    }
}
