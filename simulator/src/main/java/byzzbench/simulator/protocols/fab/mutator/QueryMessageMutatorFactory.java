package byzzbench.simulator.protocols.fab.mutator;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.fab.messages.QueryMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
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
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();

                        if (event.isEmpty()) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        if (!(messageEvent.getPayload() instanceof QueryMessage message)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        QueryMessage mutatedMessage = message.withViewNumber(
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
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        if (!(messageEvent.getPayload() instanceof QueryMessage message)) {
                            throw new IllegalArgumentException("Invalid message type");
                        }

                        QueryMessage mutatedMessage = message.withViewNumber(
                                message.getViewNumber() - 1
                        );

                        messageEvent.setPayload(mutatedMessage);
                    }
                }
        );
    }
}
