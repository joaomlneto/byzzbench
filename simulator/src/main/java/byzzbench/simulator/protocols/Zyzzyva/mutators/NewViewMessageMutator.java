package byzzbench.simulator.protocols.Zyzzyva.mutators;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.Zyzzyva.message.NewViewMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class NewViewMessageMutator extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault("zyzzyva-new-view-increment-future-view-number", "Increment View Number", List.of(NewViewMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                            throw invalidMessageTypeException;
                        }

                        NewViewMessage mutatedMessage = message.withFutureViewNumber(message.getFutureViewNumber() + 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                }, new MessageMutationFault("zyzzyva-new-view-decrement-future-view-number", "Decrement View Number", List.of(NewViewMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                            throw invalidMessageTypeException;
                        }

                        NewViewMessage mutatedMessage = message.withFutureViewNumber(message.getFutureViewNumber() - 1);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                });
    }
}
