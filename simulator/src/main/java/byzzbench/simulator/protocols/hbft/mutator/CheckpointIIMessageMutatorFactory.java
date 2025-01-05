package byzzbench.simulator.protocols.hbft.mutator;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.protocols.hbft.SpeculativeHistory;
import byzzbench.simulator.protocols.hbft.message.CheckpointIIMessage;
import byzzbench.simulator.protocols.hbft.message.RequestMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.ToString;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

@Component
@ToString
public class CheckpointIIMessageMutatorFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault("hbft-checkpointII-different-digest", "Change digest", List.of(CheckpointIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        // Create a random digest
                        byte[] digest = new byte[20];
                        CheckpointIIMessage mutatedMessage = message.withDigest(digest);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointII-seq-inc", "Increment Sequence Number", List.of(CheckpointIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIMessage message)) {
                            throw invalidMessageTypeException;
                        }

                        CheckpointIIMessage mutatedMessage = message.withLastSeqNumber(message.getLastSeqNumber() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointII-seq-dec", "Decrement Sequence Number", List.of(CheckpointIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        CheckpointIIMessage mutatedMessage = message.withLastSeqNumber(message.getLastSeqNumber() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointII-remove-last-request", "Remove last request from history", List.of(CheckpointIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = message.getHistory();
                        history.getRequests().remove(history.getRequests().lastEntry().getKey());
                        CheckpointIIMessage mutatedMessage = message.withHistory(history); //TODO: withDigest(digest(history))
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointII-remove-first-request", "Remove first request from history", List.of(CheckpointIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = message.getHistory();
                        history.getRequests().remove(history.getRequests().firstEntry().getKey());
                        CheckpointIIMessage mutatedMessage = message.withHistory(history); //TODO: withDigest(digest(history))
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointII-decrement-last-request-seq", "Decrement last request's seq num from history", List.of(CheckpointIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = message.getHistory();
                        Entry<Long, RequestMessage> lastReq = history.getRequests().lastEntry();
                        history.getRequests().remove(lastReq.getKey());
                        history.getRequests().put(lastReq.getKey() - 1, lastReq.getValue());
                        CheckpointIIMessage mutatedMessage = message.withHistory(history); //TODO: withDigest(digest(history))
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointII-increment-last-request-seq", "Increment last request's seq num from history", List.of(CheckpointIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = message.getHistory();
                        Entry<Long, RequestMessage> lastReq = history.getRequests().lastEntry();
                        history.getRequests().remove(lastReq.getKey());
                        history.getRequests().put(lastReq.getKey() + 1, lastReq.getValue());
                        CheckpointIIMessage mutatedMessage = message.withHistory(history); //TODO: withDigest(digest(history))
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointII-decrement-first-request-seq", "Decrement first request's seq num from history", List.of(CheckpointIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = message.getHistory();
                        Entry<Long, RequestMessage> firstReq = history.getRequests().firstEntry();
                        history.getRequests().remove(firstReq.getKey());
                        history.getRequests().put(firstReq.getKey() - 1, firstReq.getValue());
                        CheckpointIIMessage mutatedMessage = message.withHistory(history); //TODO: withDigest(digest(history))
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointII-increment-first-request-seq", "Increment first request's seq num from history", List.of(CheckpointIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = message.getHistory();
                        Entry<Long, RequestMessage> firstReq = history.getRequests().firstEntry();
                        history.getRequests().remove(firstReq.getKey());
                        history.getRequests().put(firstReq.getKey() + 1, firstReq.getValue());
                        CheckpointIIMessage mutatedMessage = message.withHistory(history); //TODO: withDigest(digest(history))
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                }
        );
    }
}
