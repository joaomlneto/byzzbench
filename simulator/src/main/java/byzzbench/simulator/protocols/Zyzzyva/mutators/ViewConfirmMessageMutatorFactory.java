package byzzbench.simulator.protocols.Zyzzyva.mutators;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.Zyzzyva.message.ViewConfirmMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
public class ViewConfirmMessageMutatorFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                // Small-scope mutations
                new MessageMutationFault("zyzzyva-view-confirm-message-view-inc", "Increment View Number", List.of(ViewConfirmMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewConfirmMessage message)) {
                            throw invalidMessageTypeException;
                        }

                        ViewConfirmMessage mutatedMessage = message.withFutureViewNumber(message.getFutureViewNumber() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                    }
                }, new MessageMutationFault("zyzzyva-view-confirm-message-view-dec", "Decrement View Number", List.of(ViewConfirmMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewConfirmMessage message)) {
                            throw invalidMessageTypeException;
                        }

                        ViewConfirmMessage mutatedMessage = message.withFutureViewNumber(message.getFutureViewNumber() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                    }
                }, new MessageMutationFault("zyzzyva-view-confirm-message-sequence-inc", "Increment Sequence Number", List.of(ViewConfirmMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewConfirmMessage message)) {
                            throw invalidMessageTypeException;
                        }

                        ViewConfirmMessage mutatedMessage = message.withLastKnownSequenceNumber(message.getLastKnownSequenceNumber() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                    }
                }, new MessageMutationFault("zyzzyva-view-confirm-message-sequence-dec", "Decrement Sequence Number", List.of(ViewConfirmMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewConfirmMessage message)) {
                            throw invalidMessageTypeException;
                        }

                        ViewConfirmMessage mutatedMessage = message.withLastKnownSequenceNumber(message.getLastKnownSequenceNumber() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                    }
                }
                // Any-scope mutations
                ,
                new MessageMutationFault("zyzzyva-view-confirm-message-view-inc-any", "Increment View Number Any", List.of(ViewConfirmMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewConfirmMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        Random random = new Random();
                        long randomView = random.nextLong(1, Long.MAX_VALUE);
                        ViewConfirmMessage mutatedMessage = message.withFutureViewNumber(randomView + message.getFutureViewNumber());
                        mutatedMessage.sign(message.getSignedBy());
                    }
                }, new MessageMutationFault("zyzzyva-view-confirm-message-view-dec-any", "Decrement View Number Any", List.of(ViewConfirmMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewConfirmMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        Random random = new Random();
                        long randomView = random.nextLong(1, Long.MAX_VALUE);
                        ViewConfirmMessage mutatedMessage = message.withFutureViewNumber(message.getFutureViewNumber() - randomView);
                        mutatedMessage.sign(message.getSignedBy());
                    }
                }, new MessageMutationFault("zyzzyva-view-confirm-message-sequence-inc-any", "Increment Sequence Number Any", List.of(ViewConfirmMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewConfirmMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        Random random = new Random();
                        long randomSequence = random.nextLong(1, Long.MAX_VALUE);
                        ViewConfirmMessage mutatedMessage = message.withLastKnownSequenceNumber(randomSequence + message.getLastKnownSequenceNumber());
                        mutatedMessage.sign(message.getSignedBy());
                    }
                }, new MessageMutationFault("zyzzyva-view-confirm-message-sequence-dec-any", "Decrement Sequence Number Any", List.of(ViewConfirmMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewConfirmMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        Random random = new Random();
                        long randomSequence = random.nextLong(1, Long.MAX_VALUE);
                        ViewConfirmMessage mutatedMessage = message.withLastKnownSequenceNumber(message.getLastKnownSequenceNumber() - randomSequence);
                        mutatedMessage.sign(message.getSignedBy());
                    }
                }
        );
    }
}
