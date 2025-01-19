package byzzbench.simulator.protocols.fab2.mutator;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.fab2.ProposalNumber;
import byzzbench.simulator.protocols.fab2.messages.QueryMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ToString
public class QueryMessageMutatorFactory2 extends MessageMutatorFactory {
    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
//                new MessageMutationFault(
//                        "fab-query-inc2",
//                        "Increment Query Number",
//                        List.of(QueryMessage.class)
//                ) {
//                    @Override
//                    public void accept(FaultContext serializable) {
//                        Optional<Event> event = serializable.getEvent();
//
//                        if (event.isEmpty()) {
//                            throw new IllegalArgumentException("Invalid message type");
//                        }
//
//                        if (!(event.get() instanceof MessageEvent messageEvent)) {
//                            throw new IllegalArgumentException("Invalid message type");
//                        }
//
//                        if (!(messageEvent.getPayload() instanceof QueryMessage message)) {
//                            throw new IllegalArgumentException("Invalid message type");
//                        }
//
//                        QueryMessage mutatedMessage = message.withProposalNumber(
//                                new ProposalNumber(
//                                        message.getViewNumber(),
//                                        message.getSequenceNumber() + 1
//                                )
//                        );
//
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                },
//
//                new MessageMutationFault(
//                        "fab-query-dec2",
//                        "Decrement Accept Number",
//                        List.of(QueryMessage.class)
//                ) {
//                    @Override
//                    public void accept(FaultContext serializable) {
//                        Optional<Event> event = serializable.getEvent();
//                        if (event.isEmpty()) {
//                            throw new IllegalArgumentException("Invalid message type");
//                        }
//
//                        if (!(event.get() instanceof MessageEvent messageEvent)) {
//                            throw new IllegalArgumentException("Invalid message type");
//                        }
//
//                        if (!(messageEvent.getPayload() instanceof QueryMessage message)) {
//                            throw new IllegalArgumentException("Invalid message type");
//                        }
//
//                        QueryMessage mutatedMessage = message.withProposalNumber(
//                                new ProposalNumber(
//                                        message.getViewNumber(),
//                                        message.getSequenceNumber() - 1
//                                )
//                        );
//
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                },
//
//                new MessageMutationFault(
//                        "fab-query-inc-view2",
//                        "Increment Query Number",
//                        List.of(QueryMessage.class)
//                ) {
//                    @Override
//                    public void accept(FaultContext serializable) {
//                        Optional<Event> event = serializable.getEvent();
//
//                        if (event.isEmpty()) {
//                            throw new IllegalArgumentException("Invalid message type");
//                        }
//
//                        if (!(event.get() instanceof MessageEvent messageEvent)) {
//                            throw new IllegalArgumentException("Invalid message type");
//                        }
//
//                        if (!(messageEvent.getPayload() instanceof QueryMessage message)) {
//                            throw new IllegalArgumentException("Invalid message type");
//                        }
//
//                        QueryMessage mutatedMessage = message.withProposalNumber(
//                                new ProposalNumber(
//                                        message.getViewNumber() + 1,
//                                        message.getSequenceNumber()
//                                )
//                        );
//
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                },
//
//                new MessageMutationFault(
//                        "fab-query-dec-view2",
//                        "Decrement Accept Number",
//                        List.of(QueryMessage.class)
//                ) {
//                    @Override
//                    public void accept(FaultContext serializable) {
//                        Optional<Event> event = serializable.getEvent();
//                        if (event.isEmpty()) {
//                            throw new IllegalArgumentException("Invalid message type");
//                        }
//
//                        if (!(event.get() instanceof MessageEvent messageEvent)) {
//                            throw new IllegalArgumentException("Invalid message type");
//                        }
//
//                        if (!(messageEvent.getPayload() instanceof QueryMessage message)) {
//                            throw new IllegalArgumentException("Invalid message type");
//                        }
//
//                        QueryMessage mutatedMessage = message.withProposalNumber(
//                                new ProposalNumber(
//                                        message.getViewNumber() + 1,
//                                        message.getSequenceNumber()
//                                )
//                        );
//
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                }

//                new MessageMutationFault(
//                        "fab-query-any",
//                        "Any Query Number",
//                        List.of(QueryMessage.class)
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
//                        if (!(messageEvent.getPayload() instanceof QueryMessage message)) {
//                            throw new IllegalArgumentException("Invalid message type");
//                        }
//
//                        QueryMessage mutatedMessage = message.withProposalNumber(
//                                message.getViewNumber() + mutation
//                        );
//
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                }
        );
    }
}
