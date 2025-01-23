package byzzbench.simulator.protocols.Zyzzyva.mutators;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.Zyzzyva.message.OrderedRequestMessage;
import byzzbench.simulator.protocols.Zyzzyva.message.OrderedRequestMessageWrapper;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Log
public class OrderedRequestMessageMutatorFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault("zyzzyva-ordered-request-seq-inc", "Increment Sequence Number", List.of(OrderedRequestMessageWrapper.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof OrderedRequestMessageWrapper message)) {
                            throw invalidMessageTypeException;
                        }

                        OrderedRequestMessage orm = message.getOrderedRequest();
                        OrderedRequestMessage mutatedOrm = orm.withSequenceNumber(orm.getSequenceNumber() + 1);
                        mutatedOrm.sign(orm.getSignedBy());
                        OrderedRequestMessageWrapper mutatedMessage = message.withOrderedRequest(mutatedOrm);
                        messageEvent.setPayload(mutatedMessage);
                    }
                }, new MessageMutationFault("zyzzyva-ordered-request-seq-dec", "Decrement Sequence Number", List.of(OrderedRequestMessageWrapper.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof OrderedRequestMessageWrapper message)) {
                            throw invalidMessageTypeException;
                        }
                        OrderedRequestMessage orm = message.getOrderedRequest();
                        OrderedRequestMessage mutatedOrm = orm.withSequenceNumber(orm.getSequenceNumber() - 1);
                        mutatedOrm.sign(orm.getSignedBy());
                        OrderedRequestMessageWrapper mutatedMessage = message.withOrderedRequest(mutatedOrm);
                        messageEvent.setPayload(mutatedMessage);
                    }
                }, new MessageMutationFault("zyzzyva-ordered-request-view-inc", "Increment View Number", List.of(OrderedRequestMessageWrapper.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof OrderedRequestMessageWrapper message)) {
                            throw invalidMessageTypeException;
                        }

                        OrderedRequestMessage orm = message.getOrderedRequest();
                        OrderedRequestMessage mutatedOrm = orm.withViewNumber(orm.getViewNumber() + 1);
                        mutatedOrm.sign(orm.getSignedBy());
                        OrderedRequestMessageWrapper mutatedMessage = message.withOrderedRequest(mutatedOrm);
                        messageEvent.setPayload(mutatedMessage);
                    }
                }, new MessageMutationFault("zyzzyva-ordered-request-view-dec", "Decrement View Number", List.of(OrderedRequestMessageWrapper.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof OrderedRequestMessageWrapper message)) {
                            throw invalidMessageTypeException;
                        }

                        OrderedRequestMessage orm = message.getOrderedRequest();
                        OrderedRequestMessage mutatedOrm = orm.withViewNumber(orm.getViewNumber() - 1);
                        mutatedOrm.sign(orm.getSignedBy());
                        OrderedRequestMessageWrapper mutatedMessage = message.withOrderedRequest(mutatedOrm);
                        messageEvent.setPayload(mutatedMessage);
                    }
                }, new MessageMutationFault("zyzzyva-ordered-request-history-inc", "Increment History", List.of(OrderedRequestMessageWrapper.class)) {
                    // Increment the history field of the OrderedRequestMessage, not the next history :(
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof OrderedRequestMessageWrapper message)) {
                            throw invalidMessageTypeException;
                        }

                        OrderedRequestMessage orm = message.getOrderedRequest();
                        OrderedRequestMessage mutatedOrm = orm.withHistory(orm.getHistory() + 1);
                        mutatedOrm.sign(orm.getSignedBy());
                        OrderedRequestMessageWrapper mutatedMessage = message.withOrderedRequest(mutatedOrm);
                        messageEvent.setPayload(mutatedMessage);
                    }
                }, new MessageMutationFault("zyzzyva-ordered-request-history-dec", "Decrement History", List.of(OrderedRequestMessageWrapper.class)) {
                    // Increment the history field of the OrderedRequestMessage, not the next history :(
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof OrderedRequestMessageWrapper message)) {
                            throw invalidMessageTypeException;
                        }

                        OrderedRequestMessage orm = message.getOrderedRequest();
                        OrderedRequestMessage mutatedOrm = orm.withHistory(orm.getHistory() - 1);
                        mutatedOrm.sign(orm.getSignedBy());
                        OrderedRequestMessageWrapper mutatedMessage = message.withOrderedRequest(mutatedOrm);
                        messageEvent.setPayload(mutatedMessage);
                    }
                }, new MessageMutationFault("zyzzyva-ordered-request-digest-none", "Digest none", List.of(OrderedRequestMessageWrapper.class)) {
                    // Increment the history field of the OrderedRequestMessage, not the next history :(
                    @Override
                    public void accept(FaultContext serializable) {

                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof OrderedRequestMessageWrapper message)) {
                            throw invalidMessageTypeException;
                        }

                        OrderedRequestMessage orm = message.getOrderedRequest();
                        OrderedRequestMessage mutatedOrm = orm.withDigest(new byte[0]);
                        mutatedOrm.sign(orm.getSignedBy());
                        OrderedRequestMessageWrapper mutatedMessage = message.withOrderedRequest(mutatedOrm);
                        messageEvent.setPayload(mutatedMessage);
                    }
                }
        );
    }
}
