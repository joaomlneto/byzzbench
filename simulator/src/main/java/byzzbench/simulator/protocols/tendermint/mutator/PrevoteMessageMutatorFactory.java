package byzzbench.simulator.protocols.tendermint.mutator;

import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.tendermint.message.PrevoteMessage;
import byzzbench.simulator.transport.Action;
import byzzbench.simulator.transport.MessageAction;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
@ToString
public class PrevoteMessageMutatorFactory extends MessageMutatorFactory {
    private final Random random = new Random();
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault(
                        "tendermint-prevote-height-inc",
                        "Increment Height Number",
                        List.of(PrevoteMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Action> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageAction messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof PrevoteMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        PrevoteMessage mutatedMessage = message.withHeight(message.getHeight() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("tendermint-prevote-height-dec", "Decrement Height Number", List.of(PrevoteMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Action> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageAction messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof PrevoteMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        PrevoteMessage mutatedMessage = message.withHeight(message.getHeight() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("tendermint-prevote-sequence-inc", "Increment sequence Number", List.of(PrevoteMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Action> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageAction messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof PrevoteMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        PrevoteMessage mutatedMessage = message.withSequence(message.getSequence() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("tendermint-prevote-sequence-dec", "Decrement sequence Number", List.of(PrevoteMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Action> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageAction messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof PrevoteMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        PrevoteMessage mutatedMessage = message.withSequence(message.getSequence() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                }
//                new MessageMutationFault(
//                        "tendermint-prevote-height-any",
//                        "Assign Random Height Number",
//                        List.of(PrevoteMessage.class)) {
//                    @Override
//                    public void accept(FaultContext serializable) {
//                        Optional<Event> event = serializable.getEvent();
//                        if (event.isEmpty()) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(event.get() instanceof MessageEvent messageEvent)) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(messageEvent.getPayload() instanceof PrevoteMessage message)) {
//                            throw invalidMessageTypeException;
//                        }
//                        long randomHeight = random.nextLong(1, 10000); // Adjust range as needed
//                        PrevoteMessage mutatedMessage = message.withHeight(randomHeight);
//                        mutatedMessage.sign(message.getSignedBy());
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                },
//                new MessageMutationFault(
//                        "tendermint-prevote-sequence-any",
//                        "Assign Random Sequence Number",
//                        List.of(PrevoteMessage.class)) {
//                    @Override
//                    public void accept(FaultContext serializable) {
//                        Optional<Event> event = serializable.getEvent();
//                        if (event.isEmpty()) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(event.get() instanceof MessageEvent messageEvent)) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(messageEvent.getPayload() instanceof PrevoteMessage message)) {
//                            throw invalidMessageTypeException;
//                        }
//                        long randomSequence = random.nextLong(0, 100000); // Adjust range as needed
//                        PrevoteMessage mutatedMessage = message.withSequence(randomSequence);
//                        mutatedMessage.sign(message.getSignedBy());
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                }
        );
    }
}
