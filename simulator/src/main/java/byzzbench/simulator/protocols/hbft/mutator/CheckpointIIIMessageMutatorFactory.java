package byzzbench.simulator.protocols.hbft.mutator;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.protocols.hbft.SpeculativeHistory;
import byzzbench.simulator.protocols.hbft.message.CheckpointIIIMessage;
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
public class CheckpointIIIMessageMutatorFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault("hbft-checkpointIII-different-digest", "Change digest", List.of(CheckpointIIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        // Create a random digest
                        byte[] digest = new byte[20];
                        CheckpointIIIMessage mutatedMessage = message.withDigest(digest);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointIII-seq-inc", "Increment Sequence Number", List.of(CheckpointIIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIIMessage message)) {
                            throw invalidMessageTypeException;
                        }

                        CheckpointIIIMessage mutatedMessage = message.withLastSeqNumber(message.getLastSeqNumber() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointIII-seq-dec", "Decrement Sequence Number", List.of(CheckpointIIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        CheckpointIIIMessage mutatedMessage = message.withLastSeqNumber(message.getLastSeqNumber() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointIII-remove-last-request", "Remove last request from history", List.of(CheckpointIIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = message.getHistory();
                        history.getRequests().remove(history.getRequests().lastEntry().getKey());
                        CheckpointIIIMessage mutatedMessage = message.withHistory(history); //TODO: withDigest(digest(history))
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointIII-remove-first-request", "Remove first request from history", List.of(CheckpointIIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = message.getHistory();
                        history.getRequests().remove(history.getRequests().firstEntry().getKey());
                        CheckpointIIIMessage mutatedMessage = message.withHistory(history); //TODO: withDigest(digest(history))
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointIII-decrement-last-request-seq", "Decrement last request's seq num from history", List.of(CheckpointIIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = message.getHistory();
                        Entry<Long, RequestMessage> lastReq = history.getRequests().lastEntry();
                        history.getRequests().remove(lastReq.getKey());
                        history.getRequests().put(lastReq.getKey() - 1, lastReq.getValue());
                        CheckpointIIIMessage mutatedMessage = message.withHistory(history); //TODO: withDigest(digest(history))
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointIII-increment-last-request-seq", "Increment last request's seq num from history", List.of(CheckpointIIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = message.getHistory();
                        Entry<Long, RequestMessage> lastReq = history.getRequests().lastEntry();
                        history.getRequests().remove(lastReq.getKey());
                        history.getRequests().put(lastReq.getKey() + 1, lastReq.getValue());
                        CheckpointIIIMessage mutatedMessage = message.withHistory(history); //TODO: withDigest(digest(history))
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointIII-decrement-first-request-seq", "Decrement first request's seq num from history", List.of(CheckpointIIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = message.getHistory();
                        Entry<Long, RequestMessage> firstReq = history.getRequests().firstEntry();
                        history.getRequests().remove(firstReq.getKey());
                        history.getRequests().put(firstReq.getKey() - 1, firstReq.getValue());
                        CheckpointIIIMessage mutatedMessage = message.withHistory(history); //TODO: withDigest(digest(history))
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-checkpointIII-increment-first-request-seq", "Increment first request's seq num from history", List.of(CheckpointIIIMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CheckpointIIIMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = message.getHistory();
                        Entry<Long, RequestMessage> firstReq = history.getRequests().firstEntry();
                        history.getRequests().remove(firstReq.getKey());
                        history.getRequests().put(firstReq.getKey() + 1, firstReq.getValue());
                        CheckpointIIIMessage mutatedMessage = message.withHistory(history); //TODO: withDigest(digest(history))
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                }
        );
    }
}
