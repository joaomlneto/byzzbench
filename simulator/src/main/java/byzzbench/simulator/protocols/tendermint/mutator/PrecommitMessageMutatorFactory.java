package byzzbench.simulator.protocols.tendermint.mutator;

import byzzbench.simulator.faults.FaultContext;
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
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");
    private final Random random = new Random(2137L);


    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
//                new MessageMutationFault(
//                        "tendermint-precommit-height-inc",
//                        "Increment Height Number",
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
//                        PrecommitMessage mutatedMessage = message.withHeight(message.getHeight() + 1);
//                        mutatedMessage.sign(message.getSignedBy());
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                },
//                new MessageMutationFault("tendermint-precommit-height-dec", "Decrement Height Number", List.of(PrecommitMessage.class)) {
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
//                        PrecommitMessage mutatedMessage = message.withHeight(message.getHeight() - 1);
//                        mutatedMessage.sign(message.getSignedBy());
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                },
//                new MessageMutationFault("tendermint-precommit-round-inc", "Increment Round Number", List.of(PrecommitMessage.class)) {
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
//                        PrecommitMessage mutatedMessage = message.withRound(message.getRound() + 1);
//                        mutatedMessage.sign(message.getSignedBy());
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                },
//                new MessageMutationFault("tendermint-precommit-round-dec", "Decrement Round Number", List.of(PrecommitMessage.class)) {
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
//                        PrecommitMessage mutatedMessage = message.withRound(message.getRound() - 1);
//                        mutatedMessage.sign(message.getSignedBy());
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                }
                new MessageMutationFault(
                        "tendermint-precommit-height-any",
                        "Assign Random Height Number",
                        List.of(PrecommitMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
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
                        long randomHeight = random.nextLong(1, 10000); // Adjust range as needed
                        PrecommitMessage mutatedMessage = message.withHeight(randomHeight);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault(
                        "tendermint-precommit-round-any",
                        "Assign Random Round Number",
                        List.of(PrecommitMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
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
                        long randomRound = random.nextLong(0, 100000); // Adjust range as needed
                        PrecommitMessage mutatedMessage = message.withRound(randomRound);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                }
        );
    }
}
