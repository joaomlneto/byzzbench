package byzzbench.simulator.protocols.hbft.mutator;

import java.util.Collection;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.ToString;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import byzzbench.simulator.protocols.hbft.SpeculativeHistory;
import byzzbench.simulator.protocols.hbft.message.RequestMessage;
import byzzbench.simulator.protocols.hbft.message.NewViewMessage;
import byzzbench.simulator.protocols.hbft.message.ViewChangeMessage;
import byzzbench.simulator.protocols.hbft.utils.Checkpoint;

@Component
@ToString
public class NewViewMessageFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault("hbft-new-view-view-inc", "Increment View Number",List.of(NewViewMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        NewViewMessage mutatedMessage = message.withNewViewNumber(message.getNewViewNumber() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-new-view-view-dec", "Decrement View Number", List.of(NewViewMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        NewViewMessage mutatedMessage = message.withNewViewNumber(message.getNewViewNumber() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-new-view-remove-first-view-change-proof", "Remove first view change proof", List.of(NewViewMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        Collection<ViewChangeMessage> viewChanges = message.getViewChangeProofs();
                        viewChanges.remove(viewChanges.iterator().next());
                        NewViewMessage mutatedMessage = message.withViewChangeProofs(viewChanges);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-new-view-change-a-view-change-proof", "Change a view change proof (remove and add a new one)", List.of(NewViewMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        Collection<ViewChangeMessage> viewChanges = message.getViewChangeProofs();
                        viewChanges.remove(viewChanges.iterator().next());
                        ViewChangeMessage messageToCopy = viewChanges.iterator().next();
                        ViewChangeMessage newViewChangeMessage = new ViewChangeMessage(messageToCopy.getNewViewNumber(),
                            messageToCopy.getSpeculativeHistoryP(),
                            messageToCopy.getSpeculativeHistoryQ(),
                            messageToCopy.getRequestsR(),
                            messageToCopy.getReplicaId());
                        viewChanges.add(newViewChangeMessage);
                        NewViewMessage mutatedMessage = message.withViewChangeProofs(viewChanges);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-new-view-checkpoint-empty", "Send empty checkpoint", List.of(NewViewMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        Checkpoint checkpoint = message.getCheckpoint();
                        checkpoint.setSequenceNumber(0);
                        checkpoint.setHistory(null);
                        NewViewMessage mutatedMessage = message.withCheckpoint(checkpoint);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-new-view-checkpoint-decrement-seqNum", "Decrement checkpoint seqNum", List.of(NewViewMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        Checkpoint checkpoint = message.getCheckpoint();
                        checkpoint.setSequenceNumber(checkpoint.getSequenceNumber() - 1);
                        checkpoint.setHistory(checkpoint.getHistory().getHistoryBefore(checkpoint.getSequenceNumber() - 1));
                        NewViewMessage mutatedMessage = message.withCheckpoint(checkpoint);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-new-view-checkpoint-increment-seqNum", "Increment checkpoint seqNum", List.of(NewViewMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        Checkpoint checkpoint = message.getCheckpoint();
                        checkpoint.setSequenceNumber(checkpoint.getSequenceNumber() - 1);
                        NewViewMessage mutatedMessage = message.withCheckpoint(checkpoint);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-new-view-add-request", "Add request to R", List.of(NewViewMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory requests = message.getSpeculativeHistory();
                        requests.addEntry(requests.getGreatestSeqNumber() + 1, null);
                        NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-new-view-remove-last-request", "Remove last request from R", List.of(NewViewMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory requests = message.getSpeculativeHistory();
                        requests = requests.getHistoryBefore(requests.getGreatestSeqNumber() - 1);
                        NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                }

        );
    }
}
