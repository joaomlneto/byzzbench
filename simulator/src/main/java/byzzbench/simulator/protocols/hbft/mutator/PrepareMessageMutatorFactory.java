package byzzbench.simulator.protocols.hbft.mutator;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.MessageMutationFault;
import byzzbench.simulator.faults.MessageMutatorFactory;
import byzzbench.simulator.protocols.hbft.message.PrepareMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ToString
public class PrepareMessageMutatorFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault(
                        "hbft-prepare-view-inc",
                        "Increment View Number",
                        List.of(PrepareMessage.class)) {
                            @Override
                            public void accept(FaultContext serializable) {
                                Optional<Event> event = serializable.getEvent();
                                if (event.isEmpty()) {
                                    throw invalidMessageTypeException;
                                }
                                if (!(event.get() instanceof MessageEvent messageEvent)) {
                                    throw invalidMessageTypeException;
                                }
                                if (!(messageEvent.getPayload() instanceof PrepareMessage message)) {
                                    throw invalidMessageTypeException;
                                }
                                PrepareMessage mutatedMessage = message.withViewNumber(message.getViewNumber() + 1);
                                mutatedMessage.sign(message.getSignedBy());
                                messageEvent.setPayload(mutatedMessage);
                            }
                        },
                new MessageMutationFault("hbft-prepare-view-dec", "Decrement View Number", List.of(PrepareMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof PrepareMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        PrepareMessage mutatedMessage = message.withViewNumber(message.getViewNumber() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-prepare-seq-inc", "Increment Sequence Number", List.of(PrepareMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof PrepareMessage message)) {
                            throw invalidMessageTypeException;
                        }

                        PrepareMessage mutatedMessage = message.withSequenceNumber(message.getSequenceNumber() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-prepare-sec-dec", "Decrement Sequence Number", List.of(PrepareMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof PrepareMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        PrepareMessage mutatedMessage = message.withSequenceNumber(message.getSequenceNumber() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-prepare-different-digest", "Change digest", List.of(PrepareMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof PrepareMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        // Create a random digest
                        byte[] digest = new byte[20];
                        PrepareMessage mutatedMessage = message.withDigest(digest);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                }
        );
    }
}
