package byzzbench.simulator.protocols.fab.mutator;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.fab.messages.SuspectMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ToString
public class SuspectMessageMutatorFactory extends MessageMutatorFactory {
    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault(
                        "fab-suspect-inc",
                        "Increment Suspect Number",
                        List.of(SuspectMessage.class)
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

                        if (!(messageEvent.getPayload() instanceof SuspectMessage message)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        SuspectMessage mutatedMessage = message.withViewNumber(
                                message.getViewNumber() + 1
                        );

                        messageEvent.setPayload(mutatedMessage);
                    }
                },

                new MessageMutationFault(
                        "fab-suspect-dec",
                        "Decrement Suspect Number",
                        List.of(SuspectMessage.class)
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

                        if (!(messageEvent.getPayload() instanceof SuspectMessage message)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        SuspectMessage mutatedMessage = message.withViewNumber(
                                message.getViewNumber() - 1
                        );

                        messageEvent.setPayload(mutatedMessage);
                    }
                }
        );
    }
}
