package byzzbench.simulator.protocols.fab.mutator;

import byzzbench.simulator.faults.ScenarioContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.fab.Pair;
import byzzbench.simulator.protocols.fab.messages.ProposeMessage;
import byzzbench.simulator.transport.Action;
import byzzbench.simulator.transport.MessageAction;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ToString
public class ProposeMessageMutatorFactory extends MessageMutatorFactory {
    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault(
                        "fab-propose-inc",
                        "Increment Propose Number",
                        List.of(ProposeMessage.class)
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

                        if (!(messageEvent.getPayload() instanceof ProposeMessage message)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        ProposeMessage mutatedMessage = message.withValueAndProposalNumber(
                                new Pair(message.getValueAndProposalNumber().getNumber() + 1,
                                        message.getValueAndProposalNumber().getValue())
                        );

                        messageEvent.setPayload(mutatedMessage);
                    }
                },

                new MessageMutationFault(
                        "fab-propose-dec",
                        "Decrement Propose Number",
                        List.of(ProposeMessage.class)
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

                        if (!(messageEvent.getPayload() instanceof ProposeMessage message)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        ProposeMessage mutatedMessage = message.withValueAndProposalNumber(
                                new Pair(message.getValueAndProposalNumber().getNumber() - 1,
                                        message.getValueAndProposalNumber().getValue())
                        );

                        messageEvent.setPayload(mutatedMessage);
                    }
                }

//                new MessageMutationFault(
//                        "fab-propose-any",
//                        "Any Propose Number",
//                        List.of(ProposeMessage.class)
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
//                        if (!(messageEvent.getPayload() instanceof ProposeMessage message)) {
//                            throw new IllegalArgumentException("Invalid message type");
//                        }
//
//                        ProposeMessage mutatedMessage = message.withValueAndProposalNumber(
//                                new Pair(message.getValueAndProposalNumber().getNumber() + mutation,
//                                        message.getValueAndProposalNumber().getValue())
//                        );
//
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                }
        );
    }
}
