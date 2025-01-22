package byzzbench.simulator.protocols.fab2.mutator;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.fab2.Pair;
import byzzbench.simulator.protocols.fab2.ProposalNumber;
import byzzbench.simulator.protocols.fab2.messages.LearnMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
@ToString
public class LearnMessageMutatorFactory2 extends MessageMutatorFactory{
    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault(
                        "fab-learn-inc2",
                        "Increment Learn Number",
                        List.of(LearnMessage.class)
                ) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();

                        if (event.isEmpty()) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        if (!(messageEvent.getPayload() instanceof LearnMessage message)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        LearnMessage mutatedMessage = message.withValueAndProposalNumber(
                                new Pair(message.getValueAndProposalNumber().getValue(),
                                        new ProposalNumber(
                                                message.getValueAndProposalNumber().getProposalNumber().getViewNumber(),
                                                message.getValueAndProposalNumber().getProposalNumber().getSequenceNumber() + 1
                                        ))
                        );

                        messageEvent.setPayload(mutatedMessage);
                    }
                },

                new MessageMutationFault(
                        "fab-learn-dec2",
                        "Decrement Learn Number",
                        List.of(LearnMessage.class)
                ) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();

                        if (event.isEmpty()) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        if (!(messageEvent.getPayload() instanceof LearnMessage message)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        LearnMessage mutatedMessage = message.withValueAndProposalNumber(
                                new Pair(message.getValueAndProposalNumber().getValue(),
                                        new ProposalNumber(
                                                message.getValueAndProposalNumber().getProposalNumber().getViewNumber(),
                                                message.getValueAndProposalNumber().getProposalNumber().getSequenceNumber() - 1
                                        ))
                        );

                        messageEvent.setPayload(mutatedMessage);
                    }
                }

//                new MessageMutationFault(
//                "fab-learn-any",
//                "Any Learn Number",
//                List.of(LearnMessage.class)
//        ) {
//            @Override
//            public void accept(FaultContext serializable) {
//                Optional<Event> event = serializable.getEvent();
//                Random random = new Random();
//                int mutation = random.nextInt(2, 100);
//
//                if (event.isEmpty()) {
//                    throw new IllegalArgumentException("Invalid message type");
//                }
//
//                if (!(event.get() instanceof MessageEvent messageEvent)) {
//                    throw new IllegalArgumentException("Invalid message type");
//                }
//
//                if (!(messageEvent.getPayload() instanceof LearnMessage message)) {
//                    throw new IllegalArgumentException("Invalid message type");
//                }
//
//                LearnMessage mutatedMessage = message.withValueAndProposalNumber(
//                        new Pair(message.getValueAndProposalNumber().getValue(),
//                                new ProposalNumber(
//                                        message.getValueAndProposalNumber().getProposalNumber().getViewNumber(),
//                                        message.getValueAndProposalNumber().getProposalNumber().getSequenceNumber() + mutation
//                                ))
//                );
//
//                messageEvent.setPayload(mutatedMessage);
//            }
//        }
        );
    }
}
