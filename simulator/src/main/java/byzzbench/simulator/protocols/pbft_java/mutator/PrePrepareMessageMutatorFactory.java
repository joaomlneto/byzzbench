package byzzbench.simulator.protocols.pbft_java.mutator;

import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.pbft_java.message.PrePrepareMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ToString
public class PrePrepareMessageMutatorFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault(
                        "pbft-preprepare-view-inc",
                        "Increment View Number",
                        List.of(PrePrepareMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof PrePrepareMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        PrePrepareMessage mutatedMessage = message.withViewNumber(message.getViewNumber() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("pbft-preprepare-view-dec", "Decrement View Number", List.of(PrePrepareMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof PrePrepareMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        PrePrepareMessage mutatedMessage = message.withViewNumber(message.getViewNumber() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("pbft-preprepare-seq-inc", "Increment Sequence Number", List.of(PrePrepareMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof PrePrepareMessage message)) {
                            throw invalidMessageTypeException;
                        }

                        PrePrepareMessage mutatedMessage = message.withSequenceNumber(message.getSequenceNumber() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("pbft-preprepare-sec-dec", "Decrement Sequence Number", List.of(PrePrepareMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof PrePrepareMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        PrePrepareMessage mutatedMessage = message.withSequenceNumber(message.getSequenceNumber() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("pbft-preprepare-digest-modify", "Mutate digest", List.of(PrePrepareMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof PrePrepareMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        PrePrepareMessage mutatedMessage = message.withDigest("wrong digest".getBytes());
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("pbft-preprepare-request-operation-modify", "Mutate request operation", List.of(PrePrepareMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof PrePrepareMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        PrePrepareMessage mutatedMessage = message.withRequest(message.getRequest().withOperation("wrong-op"));
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                }
        );
    }
}
