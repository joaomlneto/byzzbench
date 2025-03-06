package byzzbench.simulator.protocols.fab.mutator;

import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.fab.messages.QueryMessage;
import byzzbench.simulator.transport.Action;
import byzzbench.simulator.transport.MessageAction;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ToString
public class QueryMessageMutatorFactory extends MessageMutatorFactory {
    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault(
                        "fab-query-inc",
                        "Increment Query Number",
                        List.of(QueryMessage.class)
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

                        if (!(messageEvent.getPayload() instanceof QueryMessage message)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        QueryMessage mutatedMessage = message.withProposalNumber(
                                message.getViewNumber() + 1
                        );

                        messageEvent.setPayload(mutatedMessage);
                    }
                },

                new MessageMutationFault(
                        "fab-query-dec",
                        "Decrement Accept Number",
                        List.of(QueryMessage.class)
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

                        if (!(messageEvent.getPayload() instanceof QueryMessage message)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        QueryMessage mutatedMessage = message.withProposalNumber(
                                message.getViewNumber() - 1
                        );

                        messageEvent.setPayload(mutatedMessage);
                    }
                }

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
