package byzzbench.simulator.protocols.hbft.mutator;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.MessageMutationFault;
import byzzbench.simulator.faults.MessageMutatorFactory;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.ToString;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.logging.log4j.message.Message;

import byzzbench.simulator.protocols.hbft.SpeculativeHistory;
import byzzbench.simulator.protocols.hbft.message.RequestMessage;
import byzzbench.simulator.protocols.hbft.message.ViewChangeMessage;
import byzzbench.simulator.protocols.hbft.utils.Checkpoint;

@Component
@ToString
public class ViewChangeMessageFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault("hbft-view-change-view-inc", "Increment View Number",List.of(ViewChangeMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        ViewChangeMessage mutatedMessage = message.withNewViewNumber(message.getNewViewNumber() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-view-change-view-dec", "Decrement View Number", List.of(ViewChangeMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        ViewChangeMessage mutatedMessage = message.withNewViewNumber(message.getNewViewNumber() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-view-change-remove-send-empty-p-history", "Send empty p history", List.of(ViewChangeMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = new SpeculativeHistory();
                        ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-view-change-empty-checkpoint", "Send empty checkpoint", List.of(ViewChangeMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        Checkpoint checkpoint = message.getSpeculativeHistoryQ();
                        checkpoint.setHistory(new SpeculativeHistory());
                        ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-view-change-checkpoint-with-0-seqNum", "Send checkpoint with 0 seqNum", List.of(ViewChangeMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        Checkpoint checkpoint = message.getSpeculativeHistoryQ();
                        checkpoint.setSequenceNumber(0);
                        ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-view-change-empty-checkpoint-with-zero-seq", "Send empty checkpoint with 0 seqNum", List.of(ViewChangeMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        Checkpoint checkpoint = message.getSpeculativeHistoryQ();
                        checkpoint.setHistory(new SpeculativeHistory());
                        checkpoint.setSequenceNumber(0);
                        ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-view-change-checkpoint-decrement-seqNum", "Decrement checkpoint seqNum", List.of(ViewChangeMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        Checkpoint checkpoint = message.getSpeculativeHistoryQ();
                        checkpoint.setSequenceNumber(checkpoint.getSequenceNumber() + 1);
                        ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-view-change-checkpoint-increment-seqNum", "Increment checkpoint seqNum", List.of(ViewChangeMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        Checkpoint checkpoint = message.getSpeculativeHistoryQ();
                        checkpoint.setSequenceNumber(checkpoint.getSequenceNumber() - 1);
                        ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-view-change-empty-requests", "Empty the requests", List.of(ViewChangeMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        SortedMap<Long, RequestMessage> requests = new TreeMap<>();
                        ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-view-change-remove-last-request", "Remove last request", List.of(ViewChangeMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        SortedMap<Long, RequestMessage> requests = message.getRequestsR();
                        requests.remove(requests.lastEntry().getKey());
                        ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                }

        );
    }
}
