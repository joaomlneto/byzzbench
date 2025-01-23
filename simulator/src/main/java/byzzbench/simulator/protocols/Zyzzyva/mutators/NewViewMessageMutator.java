package byzzbench.simulator.protocols.Zyzzyva.mutators;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.Zyzzyva.message.NewViewMessage;
import byzzbench.simulator.protocols.Zyzzyva.message.ViewChangeMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.SortedMap;

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
                }, new MessageMutationFault("zyzzyva-new-view-remove-view-change-messages", "Remove View Change Message", List.of(NewViewMessage.class)) {
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
                        // ensure that we have less than 2f + 1 view change messages
                        SortedMap<String, ViewChangeMessage> viewChangeMessages = message.getViewChangeMessages().subMap("a", "b");
                        NewViewMessage mutatedMessage = message.withViewChangeMessages(viewChangeMessages);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                }, new MessageMutationFault("zyzzyva-new-view-add-view-change-messages", "Add View Change Message", List.of(NewViewMessage.class)) {
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
                        // ensure that we have less than 2f + 1 view change messages
                        SortedMap<String, ViewChangeMessage> viewChangeMessages = message.getViewChangeMessages();
                        viewChangeMessages.put("a", new ViewChangeMessage(0, 0, List.of(), null, null, "a"));
                        NewViewMessage mutatedMessage = message.withViewChangeMessages(viewChangeMessages);
                        mutatedMessage.sign(message.getSignedBy());
                        messageEvent.setPayload(mutatedMessage);
                    }
                });
    }
}
