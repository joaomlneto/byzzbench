package byzzbench.simulator.protocols.Zyzzyva.mutators;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.Zyzzyva.message.NewViewMessage;
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
                }, new MessageMutationFault("zyzzyva-view-confirm-message-history-inc", "Increment History", List.of(ViewConfirmMessage.class)) {
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

                        ViewConfirmMessage mutatedMessage = message.withHistory(message.getHistory() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                    }
                }, new MessageMutationFault("zyzzyva-view-confirm-message-history-dec", "Decrement History", List.of(ViewConfirmMessage.class)) {
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

                        ViewConfirmMessage mutatedMessage = message.withHistory(message.getHistory() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                    }
                }, new MessageMutationFault("zyzzyva-view-confirm-message-replica-id", "Change Replica Id", List.of(ViewConfirmMessage.class)) {
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
                        // randomly generate a new replica id between A and D
                        String newReplicaId = Character.valueOf((char) ('A' + random.nextInt(4))).toString();
                        while (newReplicaId.equals(message.getReplicaId())) {
                            newReplicaId = Character.valueOf((char) ('A' + random.nextInt(4))).toString();
                        }
                        ViewConfirmMessage mutatedMessage = message.withReplicaId(newReplicaId);
                        mutatedMessage.sign(message.getSignedBy());
                    }
                });
    }
}
