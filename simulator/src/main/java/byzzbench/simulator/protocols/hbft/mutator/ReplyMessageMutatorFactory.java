package byzzbench.simulator.protocols.hbft.mutator;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.protocols.hbft.message.ReplyMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
@ToString
public class ReplyMessageMutatorFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");
    Random random = new Random();
    int bound = 100;

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault("hbft-reply-view-inc", "Increment View Number", List.of(ReplyMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ReplyMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        ReplyMessage mutatedMessage = message.withViewNumber(message.getViewNumber() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-reply-view-dec", "Decrement View Number", List.of(ReplyMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ReplyMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        ReplyMessage mutatedMessage = message.withViewNumber(message.getViewNumber() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-reply-seq-inc", "Increment Sequence Number", List.of(ReplyMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ReplyMessage message)) {
                            throw invalidMessageTypeException;
                        }

                        ReplyMessage mutatedMessage = message.withSequenceNumber(message.getSequenceNumber() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-reply-sec-dec", "Decrement Sequence Number", List.of(ReplyMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ReplyMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        ReplyMessage mutatedMessage = message.withSequenceNumber(message.getSequenceNumber() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-reply-timestamp-inc", "Increment timestamp", List.of(ReplyMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ReplyMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        ReplyMessage mutatedMessage = message.withTimestamp(message.getTimestamp() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-reply-timestamp-dec", "Decrement timestamp", List.of(ReplyMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ReplyMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        ReplyMessage mutatedMessage = message.withTimestamp(message.getTimestamp() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                }

                //ANY-SCOPE mutations
                // ,new MessageMutationFault("hbft-reply-view-inc", "Increment View Number", List.of(ReplyMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ReplyMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         ReplyMessage mutatedMessage = message.withViewNumber(message.getViewNumber() + random.nextLong(bound));
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-reply-view-dec", "Decrement View Number", List.of(ReplyMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ReplyMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         ReplyMessage mutatedMessage = message.withViewNumber(message.getViewNumber() - random.nextLong(bound));
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-reply-seq-inc", "Increment Sequence Number", List.of(ReplyMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ReplyMessage message)) {
                //             throw invalidMessageTypeException;
                //         }

                //         ReplyMessage mutatedMessage = message.withSequenceNumber(message.getSequenceNumber() + random.nextLong(bound));
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-reply-sec-dec", "Decrement Sequence Number", List.of(ReplyMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ReplyMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         ReplyMessage mutatedMessage = message.withSequenceNumber(message.getSequenceNumber() - random.nextLong(bound));
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-reply-timestamp-inc", "Increment timestamp", List.of(ReplyMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ReplyMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         ReplyMessage mutatedMessage = message.withTimestamp(message.getTimestamp() + random.nextLong(bound));
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-reply-timestamp-dec", "Decrement timestamp", List.of(ReplyMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof ReplyMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         ReplyMessage mutatedMessage = message.withTimestamp(message.getTimestamp() - random.nextLong(bound));
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // }
        
        );
    }
}
