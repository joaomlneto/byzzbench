package byzzbench.simulator.protocols.hbft.mutator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.SortedMap;

import org.springframework.stereotype.Component;

import byzzbench.simulator.Replica;
import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.hbft.HbftJavaReplica;
import byzzbench.simulator.protocols.hbft.SpeculativeHistory;
import byzzbench.simulator.protocols.hbft.message.RequestMessage;
import byzzbench.simulator.protocols.hbft.message.ViewChangeMessage;
import byzzbench.simulator.protocols.hbft.utils.Checkpoint;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.ToString;

@Component
@ToString
public class ViewChangeMessageFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");
    Random random = new Random();
    int bound = 100;

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
                new MessageMutationFault("hbft-view-change-remove-last-req-checkpoint", "Remove last request from checkpoint", List.of(ViewChangeMessage.class)) {
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
                        if (checkpoint != null) {
                            SpeculativeHistory history = checkpoint.getHistory();
                            if (history != null && !history.getRequests().isEmpty()) {
                                long key = history.getRequests().lastKey();
                                history.getRequests().remove(key);
                                ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                                mutatedMessage.sign(message.getSignedBy());
                                messageEvent.setPayload(mutatedMessage);
                            }
                        }
                    }
                },
                new MessageMutationFault("hbft-view-change-remove-first-req-checkpoint", "Remove first request from checkpoint", List.of(ViewChangeMessage.class)) {
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
                        if (checkpoint != null) {
                            SpeculativeHistory history = checkpoint.getHistory();
                            if (history != null && !history.getRequests().isEmpty()) {
                                long key = history.getRequests().firstKey();
                                history.getRequests().remove(key);
                                ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                                mutatedMessage.sign(message.getSignedBy());
                                messageEvent.setPayload(mutatedMessage);
                            }
                        }
                    }
                },
                new MessageMutationFault("hbft-view-change-remove-last-req-p", "Remove last request from P", List.of(ViewChangeMessage.class)) {
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
                        if (history != null && !history.getRequests().isEmpty()) {
                            long key = history.getRequests().lastKey();
                            history.getRequests().remove(key);
                            ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
                    }
                },
                new MessageMutationFault("hbft-view-change-remove-first-req-p", "Remove first request from P", List.of(ViewChangeMessage.class)) {
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
                        if (history != null && !history.getRequests().isEmpty()) {
                            long key = history.getRequests().firstKey();
                            history.getRequests().remove(key);
                            ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
                    }
                },
                new MessageMutationFault("hbft-view-change-decrement-last-req-p", "Decrement seq num of last request in P", List.of(ViewChangeMessage.class)) {
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
                        if (history != null && !history.getRequests().isEmpty()) {
                            Entry<Long, RequestMessage> lastReq = history.getRequests().lastEntry();
                            history.getRequests().remove(lastReq.getKey());
                            history.getRequests().put(lastReq.getKey() - 1, lastReq.getValue());
                            ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
                    }
                },
                new MessageMutationFault("hbft-view-change-increment-last-req-p", "Increment seq num of last request in P", List.of(ViewChangeMessage.class)) {
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
                        if (history != null && !history.getRequests().isEmpty()) {
                            Entry<Long, RequestMessage> lastReq = history.getRequests().lastEntry();
                            history.getRequests().remove(lastReq.getKey());
                            history.getRequests().put(lastReq.getKey() + 1, lastReq.getValue());
                            ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
                    }
                },
                new MessageMutationFault("hbft-view-change-decrement-first-req-p", "Decrement seq num of first request in P", List.of(ViewChangeMessage.class)) {
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
                        if (history != null && !history.getRequests().isEmpty()) {
                            Entry<Long, RequestMessage> firstReq = history.getRequests().firstEntry();
                            history.getRequests().remove(firstReq.getKey());
                            history.getRequests().put(firstReq.getKey() - 1, firstReq.getValue());
                            ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
                        
                    }
                },
                new MessageMutationFault("hbft-view-change-increment-first-req-p", "Increment seq num of first request in P", List.of(ViewChangeMessage.class)) {
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
                        if (history != null && !history.getRequests().isEmpty()) {
                            Entry<Long, RequestMessage> firstReq = history.getRequests().firstEntry();
                            history.getRequests().remove(firstReq.getKey());
                            history.getRequests().put(firstReq.getKey() + 1, firstReq.getValue());
                            ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
                        
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
                        if (checkpoint != null) {
                            checkpoint.setSequenceNumber(checkpoint.getSequenceNumber() - 1);
                            ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
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
                        if (checkpoint != null) {
                            checkpoint.setSequenceNumber(checkpoint.getSequenceNumber() + 1);
                            ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
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
                        if (requests != null && !requests.isEmpty()) {
                            long key = requests.lastKey();
                            requests.remove(key);
                            ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
                    }
                },
                new MessageMutationFault("hbft-view-change-remove-first-request", "Remove first request", List.of(ViewChangeMessage.class)) {
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
                        if (requests != null && !requests.isEmpty()) {
                            long key = requests.firstKey();
                            requests.remove(key);
                            ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
                    }
                },
                new MessageMutationFault("hbft-view-change-decrement-last-request-seqNum", "Decrement last request seqNum", List.of(ViewChangeMessage.class)) {
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
                        if (requests != null && !requests.isEmpty()) {
                            Entry<Long, RequestMessage> lastReq = requests.lastEntry();
                            requests.remove(lastReq.getKey());
                            requests.put(lastReq.getKey() - 1, lastReq.getValue());
                            ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
                    }
                },
                new MessageMutationFault("hbft-view-change-increment-last-request-seqNum", "Increment last request seqNum", List.of(ViewChangeMessage.class)) {
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
                        if (requests != null && !requests.isEmpty()) {
                            Entry<Long, RequestMessage> lastReq = requests.lastEntry();
                            requests.remove(lastReq.getKey());
                            requests.put(lastReq.getKey() + 1, lastReq.getValue());
                            ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
                    }
                },
                new MessageMutationFault("hbft-view-change-decrement-first-request-seqNum", "Decrement first request seqNum", List.of(ViewChangeMessage.class)) {
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
                        if (requests != null && !requests.isEmpty()) {
                            Entry<Long, RequestMessage> firstReq = requests.firstEntry();
                            requests.remove(firstReq.getKey());
                            requests.put(firstReq.getKey() - 1, firstReq.getValue());
                            ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
                    }
                },
                new MessageMutationFault("hbft-view-change-increment-first-request-seqNum", "Increment first request seqNum", List.of(ViewChangeMessage.class)) {
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
                        if (requests != null && !requests.isEmpty()) {
                            Entry<Long, RequestMessage> firstReq = requests.firstEntry();
                            requests.remove(firstReq.getKey());
                            requests.put(firstReq.getKey() + 1, firstReq.getValue());
                            ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                            mutatedMessage.sign(message.getSignedBy());
                            messageEvent.setPayload(mutatedMessage);
                        }
                    }
                }
                // // ,
                // new MessageMutationFault("hbft-view-change-last-req-in-R", "Change last request in R", List.of(ViewChangeMessage.class)) {
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
                //         String senderId = messageEvent.getSenderId();
                //         Replica sender = serializable.getScenario().getReplicas().get(senderId);
                //         SortedMap<Long, RequestMessage> requests = message.getRequestsR();
                //         if (requests != null && !requests.isEmpty()) {
                //         Entry<Long, RequestMessage> lastReq = requests.lastEntry();
                //             if (sender instanceof HbftJavaReplica replica) {
                //                 SortedMap<Long, RequestMessage> specRequests = replica.getSpeculativeRequests();
                //                 for (Long key : specRequests.keySet()) {
                //                     if (!specRequests.get(key).equals(lastReq.getValue())) {
                //                         requests.remove(lastReq.getKey());
                //                         requests.put(lastReq.getKey(), specRequests.get(key));
                //                         ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                //                         mutatedMessage.sign(message.getSignedBy());
                //                         messageEvent.setPayload(mutatedMessage);
                //                         break;
                //                     }
                //                 }
                //             }
                //         }
                        
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-first-req-in-R", "Change first request in R", List.of(ViewChangeMessage.class)) {
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
                //         String senderId = messageEvent.getSenderId();
                //         Replica sender = serializable.getScenario().getReplicas().get(senderId);
                //         SortedMap<Long, RequestMessage> requests = message.getRequestsR();
                //         if (requests != null && !requests.isEmpty()) {
                //             Entry<Long, RequestMessage> firstReq = requests.firstEntry();
                //             if (sender instanceof HbftJavaReplica replica) {
                //                 SortedMap<Long, RequestMessage> specRequests = replica.getSpeculativeRequests();
                //                 for (Long key : specRequests.keySet()) {
                //                     if (!specRequests.get(key).equals(firstReq.getValue())) {
                //                         requests.remove(firstReq.getKey());
                //                         requests.put(firstReq.getKey(), specRequests.get(key));
                //                         ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                //                         mutatedMessage.sign(message.getSignedBy());
                //                         messageEvent.setPayload(mutatedMessage);
                //                         break;
                //                     }
                //                 }
                //             }
                //         }
                        
                //     }
                // }
                // TODO: be able to change the first or last request


                // ANY-SCOPE mutations
                //,
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
                //         ViewChangeMessage mutatedMessage = message.withNewViewNumber(message.getNewViewNumber() + random.nextLong(bound));
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
                //         ViewChangeMessage mutatedMessage = message.withNewViewNumber(message.getNewViewNumber() - random.nextLong(bound));
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-remove-random-req-checkpoint", "Remove random request from checkpoint", List.of(ViewChangeMessage.class)) {
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
                //         if (checkpoint != null) {
                //             SpeculativeHistory history = checkpoint.getHistory();
                //             if (history != null && !history.getRequests().isEmpty()) {
                //                 List<Long> keysetAsArray = new ArrayList<Long>(checkpoint.getHistory().getRequests().keySet());
                //                 checkpoint.getHistory().getRequests().remove(keysetAsArray.get(random.nextInt(keysetAsArray.size())));
                //                 var mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                //                 mutatedMessage.sign(message.getSignedBy());
                //                 messageEvent.setPayload(mutatedMessage);
                //             }
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-remove-random-req-p", "Remove random request from P", List.of(ViewChangeMessage.class)) {
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
                //         if (history != null && !history.getRequests().isEmpty()) {
                //             List<Long> keysetAsArray = new ArrayList<Long>(history.getRequests().keySet());
                //             history.getRequests().remove(keysetAsArray.get(random.nextInt(keysetAsArray.size())));
                //             ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-decrement-random-req-p", "Decrement seq num of random request in P", List.of(ViewChangeMessage.class)) {
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
                //         if (history != null && !history.getRequests().isEmpty()) {
                //             List<Long> keysetAsArray = new ArrayList<Long>(history.getRequests().keySet());
                //             Long reqKey = keysetAsArray.get(random.nextInt(keysetAsArray.size()));
                //             RequestMessage reqValue = history.getRequests().get(reqKey);
                //             history.getRequests().remove(reqKey);
                //             history.getRequests().put(reqKey - random.nextLong(bound), reqValue);
                //             ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-increment-random-req-p", "Increment seq num of random request in P", List.of(ViewChangeMessage.class)) {
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
                //         if (history != null && !history.getRequests().isEmpty()) {
                //             List<Long> keysetAsArray = new ArrayList<Long>(history.getRequests().keySet());
                //             Long reqKey = keysetAsArray.get(random.nextInt(keysetAsArray.size()));
                //             RequestMessage reqValue = history.getRequests().get(reqKey);
                //             history.getRequests().remove(reqKey);
                //             history.getRequests().put(reqKey + random.nextLong(bound), reqValue);
                //             ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryP(history);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
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
                //         if (checkpoint != null) {
                //             checkpoint.setSequenceNumber(checkpoint.getSequenceNumber() - random.nextLong(bound));
                //             ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
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
                //         if (checkpoint != null) {
                //             checkpoint.setSequenceNumber(checkpoint.getSequenceNumber() + random.nextLong(bound));
                //             ViewChangeMessage mutatedMessage = message.withSpeculativeHistoryQ(checkpoint);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-remove-random-request", "Remove random request", List.of(ViewChangeMessage.class)) {
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
                //         if (requests != null && !requests.isEmpty()) {
                //             List<Long> keysetAsArray = new ArrayList<Long>(requests.keySet());
                //             requests.remove(keysetAsArray.get(random.nextInt(keysetAsArray.size())));
                //             ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-decrement-random-request-seqNum", "Decrement random request seqNum", List.of(ViewChangeMessage.class)) {
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
                //         if (requests != null && !requests.isEmpty()) {
                //             List<Long> keysetAsArray = new ArrayList<Long>(requests.keySet());
                //             Long reqKey = keysetAsArray.get(random.nextInt(keysetAsArray.size()));
                //             RequestMessage reqValue = requests.get(reqKey);
                //             requests.remove(reqKey);
                //             requests.put(reqKey - random.nextLong(bound), reqValue);
                //             ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-view-change-increment-random-request-seqNum", "Increment random request seqNum", List.of(ViewChangeMessage.class)) {
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
                //         if (requests != null && !requests.isEmpty()) {
                //             List<Long> keysetAsArray = new ArrayList<Long>(requests.keySet());
                //             Long reqKey = keysetAsArray.get(random.nextInt(keysetAsArray.size()));
                //             RequestMessage reqValue = requests.get(reqKey);
                //             requests.remove(reqKey);
                //             requests.put(reqKey + random.nextLong(bound), reqValue);
                //             ViewChangeMessage mutatedMessage = message.withRequestsR(requests);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // }

        );
    }
}
