package byzzbench.simulator.protocols.hbft.mutator;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.ToString;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
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
    Random random = new Random();
    int bound = 100;

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                // new MessageMutationFault("hbft-view-change-view-inc", "Increment View Number",List.of(ViewChangeMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         ViewChangeMessage mutatedMessage = message.withNewViewNumber(message.getNewViewNumber() + 1);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-view-dec", "Decrement View Number", List.of(ViewChangeMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         ViewChangeMessage mutatedMessage = message.withNewViewNumber(message.getNewViewNumber() - 1);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-remove-last-req-checkpoint", "Remove last request from checkpoint", List.of(ViewChangeMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Checkpoint checkpoint = message.getSpeculativeHistoryQ();
                //         checkpoint.getHistory().getRequests().remove(checkpoint.getHistory().getRequests().lastEntry().getKey());
                //         ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-remove-first-req-checkpoint", "Remove first request from checkpoint", List.of(ViewChangeMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Checkpoint checkpoint = message.getSpeculativeHistoryQ();
                //         checkpoint.getHistory().getRequests().remove(checkpoint.getHistory().getRequests().firstEntry().getKey());
                //         ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-remove-last-req-p", "Remove last request from P", List.of(ViewChangeMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory history = message.getSpeculativeHistoryP();
                //         history.getRequests().remove(history.getRequests().lastEntry().getKey());
                //         ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-remove-first-req-p", "Remove first request from P", List.of(ViewChangeMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory history = message.getSpeculativeHistoryP();
                //         history.getRequests().remove(history.getRequests().firstEntry().getKey());
                //         ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-decrement-last-req-p", "Decrement seq num of last request in P", List.of(ViewChangeMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory history = message.getSpeculativeHistoryP();
                //         Entry<Long, RequestMessage> lastReq = history.getRequests().lastEntry();
                //         history.getRequests().remove(lastReq.getKey());
                //         history.getRequests().put(lastReq.getKey() - 1, lastReq.getValue());
                //         ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-increment-last-req-p", "Increment seq num of last request in P", List.of(ViewChangeMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory history = message.getSpeculativeHistoryP();
                //         Entry<Long, RequestMessage> lastReq = history.getRequests().lastEntry();
                //         history.getRequests().remove(lastReq.getKey());
                //         history.getRequests().put(lastReq.getKey() + 1, lastReq.getValue());
                //         ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-decrement-first-req-p", "Decrement seq num of first request in P", List.of(ViewChangeMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory history = message.getSpeculativeHistoryP();
                //         Entry<Long, RequestMessage> firstReq = history.getRequests().firstEntry();
                //         history.getRequests().remove(firstReq.getKey());
                //         history.getRequests().put(firstReq.getKey() - 1, firstReq.getValue());
                //         ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-increment-first-req-p", "Increment seq num of first request in P", List.of(ViewChangeMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory history = message.getSpeculativeHistoryP();
                //         Entry<Long, RequestMessage> firstReq = history.getRequests().firstEntry();
                //         history.getRequests().remove(firstReq.getKey());
                //         history.getRequests().put(firstReq.getKey() + 1, firstReq.getValue());
                //         ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // // new MessageMutationFault("hbft-view-change-remove-send-empty-p-history", "Send empty p history", List.of(ViewChangeMessage.class)) {
                // //     @Override
                // //     public void accept(FaultContext serializable) {
                // //         Optional<Event> event = serializable.getEvent();
                // //         if (event.isEmpty()) {
                // //             throw invalidMessageTypeException;
                // //         }
                // //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                // //             throw invalidMessageTypeException;
                // //         }
                // //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                // //             throw invalidMessageTypeException;
                // //         }
                // //         SpeculativeHistory history = new SpeculativeHistory();
                // //         ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
                // //         mutatedMessage.sign(message.getSignedBy());
                // //         messageEvent.setPayload(mutatedMessage);
                // //     }
                // // },
                // // new MessageMutationFault("hbft-view-change-empty-checkpoint", "Send empty checkpoint", List.of(ViewChangeMessage.class)) {
                // //     @Override
                // //     public void accept(FaultContext serializable) {
                // //         Optional<Event> event = serializable.getEvent();
                // //         if (event.isEmpty()) {
                // //             throw invalidMessageTypeException;
                // //         }
                // //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                // //             throw invalidMessageTypeException;
                // //         }
                // //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                // //             throw invalidMessageTypeException;
                // //         }
                // //         Checkpoint checkpoint = message.getSpeculativeHistoryQ();
                // //         checkpoint.setHistory(new SpeculativeHistory());
                // //         ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                // //         mutatedMessage.sign(message.getSignedBy());
                // //         messageEvent.setPayload(mutatedMessage);
                // //     }
                // // },
                // // new MessageMutationFault("hbft-view-change-checkpoint-with-0-seqNum", "Send checkpoint with 0 seqNum", List.of(ViewChangeMessage.class)) {
                // //     @Override
                // //     public void accept(FaultContext serializable) {
                // //         Optional<Event> event = serializable.getEvent();
                // //         if (event.isEmpty()) {
                // //             throw invalidMessageTypeException;
                // //         }
                // //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                // //             throw invalidMessageTypeException;
                // //         }
                // //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                // //             throw invalidMessageTypeException;
                // //         }
                // //         Checkpoint checkpoint = message.getSpeculativeHistoryQ();
                // //         checkpoint.setSequenceNumber(0);
                // //         ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                // //         mutatedMessage.sign(message.getSignedBy());
                // //         messageEvent.setPayload(mutatedMessage);
                // //     }
                // // },
                // // new MessageMutationFault("hbft-view-change-empty-checkpoint-with-zero-seq", "Send empty checkpoint with 0 seqNum", List.of(ViewChangeMessage.class)) {
                // //     @Override
                // //     public void accept(FaultContext serializable) {
                // //         Optional<Event> event = serializable.getEvent();
                // //         if (event.isEmpty()) {
                // //             throw invalidMessageTypeException;
                // //         }
                // //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                // //             throw invalidMessageTypeException;
                // //         }
                // //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                // //             throw invalidMessageTypeException;
                // //         }
                // //         Checkpoint checkpoint = message.getSpeculativeHistoryQ();
                // //         checkpoint.setHistory(new SpeculativeHistory());
                // //         checkpoint.setSequenceNumber(0);
                // //         ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                // //         mutatedMessage.sign(message.getSignedBy());
                // //         messageEvent.setPayload(mutatedMessage);
                // //     }
                // // },
                // new MessageMutationFault("hbft-view-change-checkpoint-decrement-seqNum", "Decrement checkpoint seqNum", List.of(ViewChangeMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Checkpoint checkpoint = message.getSpeculativeHistoryQ();
                //         checkpoint.setSequenceNumber(checkpoint.getSequenceNumber() - 1);
                //         ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-checkpoint-increment-seqNum", "Increment checkpoint seqNum", List.of(ViewChangeMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Checkpoint checkpoint = message.getSpeculativeHistoryQ();
                //         checkpoint.setSequenceNumber(checkpoint.getSequenceNumber() + 1);
                //         ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-remove-last-request", "Remove last request", List.of(ViewChangeMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SortedMap<Long, RequestMessage> requests = message.getRequestsR();
                //         requests.remove(requests.lastEntry().getKey());
                //         ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-remove-first-request", "Remove first request", List.of(ViewChangeMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SortedMap<Long, RequestMessage> requests = message.getRequestsR();
                //         requests.remove(requests.firstEntry().getKey());
                //         ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-decrement-last-request-seqNum", "Decrement last request seqNum", List.of(ViewChangeMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SortedMap<Long, RequestMessage> requests = message.getRequestsR();
                //         Entry<Long, RequestMessage> lastReq = requests.lastEntry();
                //         requests.remove(lastReq.getKey());
                //         requests.put(lastReq.getKey() - 1, lastReq.getValue());
                //         ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-increment-last-request-seqNum", "Increment last request seqNum", List.of(ViewChangeMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SortedMap<Long, RequestMessage> requests = message.getRequestsR();
                //         Entry<Long, RequestMessage> lastReq = requests.lastEntry();
                //         requests.remove(lastReq.getKey());
                //         requests.put(lastReq.getKey() + 1, lastReq.getValue());
                //         ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-decrement-first-request-seqNum", "Decrement first request seqNum", List.of(ViewChangeMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SortedMap<Long, RequestMessage> requests = message.getRequestsR();
                //         Entry<Long, RequestMessage> firstReq = requests.firstEntry();
                //         requests.remove(firstReq.getKey());
                //         requests.put(firstReq.getKey() - 1, firstReq.getValue());
                //         ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-increment-first-request-seqNum", "Increment first request seqNum", List.of(ViewChangeMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ViewChangeMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SortedMap<Long, RequestMessage> requests = message.getRequestsR();
                //         Entry<Long, RequestMessage> firstReq = requests.firstEntry();
                //         requests.remove(firstReq.getKey());
                //         requests.put(firstReq.getKey() + 1, firstReq.getValue());
                //         ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // }
                // //TODO: be able to change the first or last request


                // ANY-SCOPE mutations
                //,
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
                        ViewChangeMessage mutatedMessage = message.withNewViewNumber(message.getNewViewNumber() + random.nextLong(bound));
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
                        ViewChangeMessage mutatedMessage = message.withNewViewNumber(message.getNewViewNumber() - random.nextLong(bound));
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-view-change-remove-random-req-checkpoint", "Remove random request from checkpoint", List.of(ViewChangeMessage.class)) {
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
                        List<Long> keysetAsArray = new ArrayList<Long>(checkpoint.getHistory().getRequests().keySet());
                        checkpoint.getHistory().getRequests().remove(keysetAsArray.get(random.nextInt(keysetAsArray.size())));
                        var mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-view-change-remove-random-req-p", "Remove random request from P", List.of(ViewChangeMessage.class)) {
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
                        SpeculativeHistory history = message.getSpeculativeHistoryP();
                        List<Long> keysetAsArray = new ArrayList<Long>(history.getRequests().keySet());
                        history.getRequests().remove(keysetAsArray.get(random.nextInt(keysetAsArray.size())));
                        ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-view-change-decrement-random-req-p", "Decrement seq num of random request in P", List.of(ViewChangeMessage.class)) {
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
                        SpeculativeHistory history = message.getSpeculativeHistoryP();
                        List<Long> keysetAsArray = new ArrayList<Long>(history.getRequests().keySet());
                        Long reqKey = keysetAsArray.get(random.nextInt(keysetAsArray.size()));
                        RequestMessage reqValue = history.getRequests().get(reqKey);
                        history.getRequests().remove(reqKey);
                        history.getRequests().put(reqKey - random.nextLong(bound), reqValue);
                        ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-view-change-increment-random-req-p", "Increment seq num of random request in P", List.of(ViewChangeMessage.class)) {
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
                        SpeculativeHistory history = message.getSpeculativeHistoryP();
                        List<Long> keysetAsArray = new ArrayList<Long>(history.getRequests().keySet());
                        Long reqKey = keysetAsArray.get(random.nextInt(keysetAsArray.size()));
                        RequestMessage reqValue = history.getRequests().get(reqKey);
                        history.getRequests().remove(reqKey);
                        history.getRequests().put(reqKey + random.nextLong(bound), reqValue);
                        ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
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
                        checkpoint.setSequenceNumber(checkpoint.getSequenceNumber() - random.nextLong(bound));
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
                        checkpoint.setSequenceNumber(checkpoint.getSequenceNumber() + random.nextLong(bound));
                        ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-view-change-remove-random-request", "Remove random request", List.of(ViewChangeMessage.class)) {
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
                        List<Long> keysetAsArray = new ArrayList<Long>(requests.keySet());
                        requests.remove(keysetAsArray.get(random.nextInt(keysetAsArray.size())));
                        ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-view-change-decrement-random-request-seqNum", "Decrement random request seqNum", List.of(ViewChangeMessage.class)) {
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
                        List<Long> keysetAsArray = new ArrayList<Long>(requests.keySet());
                        Long reqKey = keysetAsArray.get(random.nextInt(keysetAsArray.size()));
                        RequestMessage reqValue = requests.get(reqKey);
                        requests.remove(reqKey);
                        requests.put(reqKey - random.nextLong(bound), reqValue);
                        ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-view-change-increment-random-request-seqNum", "Increment random request seqNum", List.of(ViewChangeMessage.class)) {
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
                        List<Long> keysetAsArray = new ArrayList<Long>(requests.keySet());
                        Long reqKey = keysetAsArray.get(random.nextInt(keysetAsArray.size()));
                        RequestMessage reqValue = requests.get(reqKey);
                        requests.remove(reqKey);
                        requests.put(reqKey + random.nextLong(bound), reqValue);
                        ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                }

        );
    }
}
