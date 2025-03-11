package byzzbench.simulator.protocols.hbft.mutator;

import byzzbench.simulator.Node;
import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.hbft.HbftJavaReplica;
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
import java.util.Random;

@Component
@ToString
public class CheckpointIIMessageMutatorFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");
    Random random = new Random();
    int bound = 100;

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault("hbft-checkpointII-seq-inc", "Increment Sequence Number", List.of(CheckpointIIMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
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
                    public void accept(ScenarioContext serializable) {
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
                    public void accept(ScenarioContext serializable) {
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
                        Node sender = serializable.getScenario().getNode(messageEvent.getSenderId());
                        if (!(sender instanceof HbftJavaReplica hbftReplica)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = message.getHistory();
                        if (history != null && !history.getRequests().isEmpty()) {
                            long key = history.getRequests().lastKey();
                            history.getRequests().remove(key);
                            byte[] digest = hbftReplica.digest(history);
                            CheckpointIIMessage mutatedMessage = message.withHistory(history).withDigest(digest);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
                    }
                },
                new MessageMutationFault("hbft-checkpointII-remove-first-request", "Remove first request from history", List.of(CheckpointIIMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
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
                        Node sender = serializable.getScenario().getNode(messageEvent.getSenderId());
                        if (!(sender instanceof HbftJavaReplica hbftReplica)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = message.getHistory();
                        if (history != null && !history.getRequests().isEmpty()) {
                            long key = history.getRequests().firstKey();
                            history.getRequests().remove(history.getRequests().firstEntry().getKey());
                            byte[] digest = hbftReplica.digest(history);
                            CheckpointIIMessage mutatedMessage = message.withHistory(history).withDigest(digest);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
                    }
                }
                ,
                new MessageMutationFault("hbft-checkpointII-decrement-last-request-seq", "Decrement last request's seq num from history", List.of(CheckpointIIMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
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
                        Node sender = serializable.getScenario().getNode(messageEvent.getSenderId());
                        if (!(sender instanceof HbftJavaReplica hbftReplica)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = message.getHistory();
                        if (history != null && !history.getRequests().isEmpty()) {
                            Entry<Long, RequestMessage> lastReq = history.getRequests().lastEntry();
                            history.getRequests().remove(lastReq.getKey());
                            history.getRequests().put(lastReq.getKey() - 1, lastReq.getValue());
                            byte[] digest = hbftReplica.digest(history);
                            CheckpointIIMessage mutatedMessage = message.withHistory(history).withDigest(digest);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
                    }
                },
                new MessageMutationFault("hbft-checkpointII-increment-last-request-seq", "Increment last request's seq num from history", List.of(CheckpointIIMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
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
                        Node sender = serializable.getScenario().getNode(messageEvent.getSenderId());
                        if (!(sender instanceof HbftJavaReplica hbftReplica)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = message.getHistory();
                        if (history != null && !history.getRequests().isEmpty()) {
                            Entry<Long, RequestMessage> lastReq = history.getRequests().lastEntry();
                            history.getRequests().remove(lastReq.getKey());
                            history.getRequests().put(lastReq.getKey() + 1, lastReq.getValue());
                            byte[] digest = hbftReplica.digest(history);
                            CheckpointIIMessage mutatedMessage = message.withHistory(history).withDigest(digest);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
                    }
                },
                new MessageMutationFault("hbft-checkpointII-decrement-first-request-seq", "Decrement first request's seq num from history", List.of(CheckpointIIMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
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
                        Node sender = serializable.getScenario().getNode(messageEvent.getSenderId());
                        if (!(sender instanceof HbftJavaReplica hbftReplica)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = message.getHistory();
                        if (history != null && !history.getRequests().isEmpty()) {
                            Entry<Long, RequestMessage> firstReq = history.getRequests().firstEntry();
                            history.getRequests().remove(firstReq.getKey());
                            history.getRequests().put(firstReq.getKey() - 1, firstReq.getValue());
                            byte[] digest = hbftReplica.digest(history);
                            CheckpointIIMessage mutatedMessage = message.withHistory(history).withDigest(digest);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
                    }
                },
                new MessageMutationFault("hbft-checkpointII-increment-first-request-seq", "Increment first request's seq num from history", List.of(CheckpointIIMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
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
                        Node sender = serializable.getScenario().getNode(messageEvent.getSenderId());
                        if (!(sender instanceof HbftJavaReplica hbftReplica)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = message.getHistory();
                        if (history != null && !history.getRequests().isEmpty()) {
                            Entry<Long, RequestMessage> firstReq = history.getRequests().firstEntry();
                            history.getRequests().remove(firstReq.getKey());
                            history.getRequests().put(firstReq.getKey() + 1, firstReq.getValue());
                            byte[] digest = hbftReplica.digest(history);
                            CheckpointIIMessage mutatedMessage = message.withHistory(history).withDigest(digest);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
                    }
                }


                // ANY-SCOPE mutations
                // ,
                // new MessageMutationFault("hbft-checkpointII-seq-inc", "Increment Sequence Number", List.of(CheckpointIIMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof CheckpointIIMessage message)) {
                //             throw invalidMessageTypeException;
                //         }

                //         CheckpointIIMessage mutatedMessage = message.withLastSeqNumber(message.getLastSeqNumber() + random.nextLong(bound));
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-checkpointII-seq-dec", "Decrement Sequence Number", List.of(CheckpointIIMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof CheckpointIIMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         CheckpointIIMessage mutatedMessage = message.withLastSeqNumber(message.getLastSeqNumber() - random.nextLong(bound));
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-checkpointII-remove-random-request", "Remove random request from history", List.of(CheckpointIIMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof CheckpointIIMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Node sender = serializable.getScenario().getNode(messageEvent.getSenderId());
                //         if (!(sender instanceof HbftJavaReplica hbftReplica)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory history = message.getHistory();
                //         if (history != null && !history.getRequests().isEmpty()) {
                //             List<Long> keysetAsArray = new ArrayList<Long>(history.getRequests().keySet());
                //             history.getRequests().remove(keysetAsArray.get(random.nextInt(keysetAsArray.size())));
                //             byte[] digest = hbftReplica.digest(history);
                //             CheckpointIIMessage mutatedMessage = message.withHistory(history).withDigest(digest);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-checkpointII-decrement-random-request-seq", "Decrement random request's seq num from history", List.of(CheckpointIIMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof CheckpointIIMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Node sender = serializable.getScenario().getNode(messageEvent.getSenderId());
                //         if (!(sender instanceof HbftJavaReplica hbftReplica)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory history = message.getHistory();
                //         if (history != null && !history.getRequests().isEmpty()) {
                //             List<Long> keysetAsArray = new ArrayList<Long>(history.getRequests().keySet());
                //             Long reqKey = keysetAsArray.get(random.nextInt(keysetAsArray.size()));
                //             RequestMessage reqValue = history.getRequests().get(reqKey);
                //             history.getRequests().remove(reqKey);
                //             history.getRequests().put(reqKey - random.nextLong(bound), reqValue);
                //             byte[] digest = hbftReplica.digest(history);
                //             CheckpointIIMessage mutatedMessage = message.withHistory(history).withDigest(digest);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-checkpointII-increment-random-request-seq", "Increment random request's seq num from history", List.of(CheckpointIIMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof CheckpointIIMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Node sender = serializable.getScenario().getNode(messageEvent.getSenderId());
                //         if (!(sender instanceof HbftJavaReplica hbftReplica)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory history = message.getHistory();
                //         if (history != null && !history.getRequests().isEmpty()) {
                //             List<Long> keysetAsArray = new ArrayList<Long>(history.getRequests().keySet());
                //             Long reqKey = keysetAsArray.get(random.nextInt(keysetAsArray.size()));
                //             RequestMessage reqValue = history.getRequests().get(reqKey);
                //             history.getRequests().remove(reqKey);
                //             history.getRequests().put(reqKey + random.nextLong(bound), reqValue);
                //             byte[] digest = hbftReplica.digest(history);
                //             CheckpointIIMessage mutatedMessage = message.withHistory(history).withDigest(digest);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // }

        );
    }
}
