package byzzbench.simulator.protocols.hbft.mutator;

import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.hbft.SpeculativeHistory;
import byzzbench.simulator.protocols.hbft.message.CommitMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ToString
public class CommitMessageMutatorFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");
    int bound = 100;

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault("hbft-commit-view-inc", "Increment View Number", List.of(CommitMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CommitMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        CommitMessage mutatedMessage = message.withViewNumber(message.getViewNumber() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-commit-view-dec", "Decrement View Number", List.of(CommitMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CommitMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        CommitMessage mutatedMessage = message.withViewNumber(message.getViewNumber() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-commit-seq-inc", "Increment Sequence Number", List.of(CommitMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CommitMessage message)) {
                            throw invalidMessageTypeException;
                        }

                        CommitMessage mutatedMessage = message.withSequenceNumber(message.getSequenceNumber() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-commit-sec-dec", "Decrement Sequence Number", List.of(CommitMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CommitMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        CommitMessage mutatedMessage = message.withSequenceNumber(message.getSequenceNumber() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-commit-different-digest", "Change digest", List.of(CommitMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CommitMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        // Create a random digest
                        byte[] digest = new byte[20];
                        CommitMessage mutatedMessage = message.withDigest(digest);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault("hbft-commit-remove-last-request-from-history", "Remove last request from history", List.of(CommitMessage.class)) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof CommitMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        SpeculativeHistory history = message.getSpeculativeHistory().getHistoryBefore(message.getSequenceNumber());
                        CommitMessage mutatedMessage = message.withSpeculativeHistory(history);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                }

                // ANY-SCOPE mutations
                //,
                // new MessageMutationFault("hbft-commit-view-inc", "Increment View Number",List.of(CommitMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof CommitMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         CommitMessage mutatedMessage = message.withViewNumber(message.getViewNumber() + random.nextLong(bound));
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-commit-view-dec", "Decrement View Number", List.of(CommitMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof CommitMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         CommitMessage mutatedMessage = message.withViewNumber(message.getViewNumber() - random.nextLong(bound));
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-commit-seq-inc", "Increment Sequence Number", List.of(CommitMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof CommitMessage message)) {
                //             throw invalidMessageTypeException;
                //         }

                //         CommitMessage mutatedMessage = message.withSequenceNumber(message.getSequenceNumber() + random.nextLong(bound));
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-commit-sec-dec", "Decrement Sequence Number", List.of(CommitMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof CommitMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         CommitMessage mutatedMessage = message.withSequenceNumber(message.getSequenceNumber() - random.nextLong(bound));
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // }

        );
    }
}
