package byzzbench.simulator.protocols.fab2.mutator;

import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.fab2.ProposalNumber;
import byzzbench.simulator.protocols.fab2.messages.PullMessage;
import byzzbench.simulator.transport.Action;
import byzzbench.simulator.transport.MessageAction;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ToString
public class PullMessageMutatorFactory2 extends MessageMutatorFactory {
    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault(
                        "fab-pull-inc2",
                        "Increment Pull Number",
                        List.of(PullMessage.class)
                ) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Action> event = serializable.getEvent();

                        if (event.isEmpty()) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        if (!(event.get() instanceof MessageAction messageEvent)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        if (!(messageEvent.getPayload() instanceof PullMessage message)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        PullMessage mutatedMessage = message.withProposalNumber(
                                new ProposalNumber(
                                        message.getViewNumber(),
                                        message.getSequenceNumber() + 1
                                )
                        );

                        messageEvent.setPayload(mutatedMessage);
                    }
                },

                new MessageMutationFault(
                        "fab-pull-dec2",
                        "Decrement Pull Number",
                        List.of(PullMessage.class)
                ) {
                    @Override
                    public void accept(ScenarioContext serializable) {
                        Optional<Action> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        if (!(event.get() instanceof MessageAction messageEvent)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        if (!(messageEvent.getPayload() instanceof PullMessage message)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        PullMessage mutatedMessage = message.withProposalNumber(
                                new ProposalNumber(
                                        message.getViewNumber(),
                                        message.getSequenceNumber() - 1
                                )
                        );

                        messageEvent.setPayload(mutatedMessage);
                    }
                }

//                new MessageMutationFault(
//                        "fab-pull-any",
//                        "Any Pull Number",
//                        List.of(PullMessage.class)
//                ) {
//                    @Override
//                    public void accept(FaultContext serializable) {
//                        Optional<Event> event = serializable.getEvent();
//                        Random random = new Random();
//                        int mutation = random.nextInt(2, 100);
//
//                        if (event.isEmpty()) {
//                            throw new IllegalArgumentException("Invalid message type");
//                        }
//
//                        if (!(event.get() instanceof MessageEvent messageEvent)) {
//                            throw new IllegalArgumentException("Invalid message type");
//                        }
//
//                        if (!(messageEvent.getPayload() instanceof PullMessage message)) {
//                            throw new IllegalArgumentException("Invalid message type");
//                        }
//
//                        PullMessage mutatedMessage = message.withProposalNumber(
//                                new ProposalNumber(
//                                        message.getViewNumber(),
//                                        message.getSequenceNumber() + mutation
//                                )
//                        );
//
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                }
        );
    }
}
