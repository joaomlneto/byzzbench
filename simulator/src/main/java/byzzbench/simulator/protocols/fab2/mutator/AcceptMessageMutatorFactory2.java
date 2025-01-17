package byzzbench.simulator.protocols.fab2.mutator;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.fab.Pair;
import byzzbench.simulator.protocols.fab.messages.AcceptMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
@ToString
public class AcceptMessageMutatorFactory2 extends MessageMutatorFactory {
    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                // Small-scope
                new MessageMutationFault(
                        "fab-accept-inc2",
                        "Increment Accept Number",
                        List.of(AcceptMessage.class)
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

                        if (!(messageEvent.getPayload() instanceof AcceptMessage message)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        AcceptMessage mutatedMessage = message.withValueAndProposalNumber(
                                new Pair(message.getValueAndProposalNumber().getNumber() + 1,
                                        message.getValueAndProposalNumber().getValue())
                        );

                        messageEvent.setPayload(mutatedMessage);
                    }
                },

                new MessageMutationFault(
                        "fab-accept-dec2",
                        "Decrement Accept Number",
                        List.of(AcceptMessage.class)
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

                        if (!(messageEvent.getPayload() instanceof AcceptMessage message)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        AcceptMessage mutatedMessage = message.withValueAndProposalNumber(
                                new Pair(message.getValueAndProposalNumber().getNumber() - 1,
                                        message.getValueAndProposalNumber().getValue())
                        );

                        messageEvent.setPayload(mutatedMessage);
                    }
                },

                // Any-scope
                new MessageMutationFault(
                        "fab-accept-any2",
                        "any-scope AcceptMessage mutation",
                        List.of(AcceptMessage.class)
                ) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        Random random = new Random();
                        int mutation = random.nextInt(2, 100);

                        if (event.isEmpty()) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        if (!(messageEvent.getPayload() instanceof AcceptMessage message)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        AcceptMessage mutatedMessage = message.withValueAndProposalNumber(
                                new Pair(message.getValueAndProposalNumber().getNumber() + mutation,
                                        message.getValueAndProposalNumber().getValue())
                        );

                        messageEvent.setPayload(mutatedMessage);
                    }
                }
        );
    }
}
