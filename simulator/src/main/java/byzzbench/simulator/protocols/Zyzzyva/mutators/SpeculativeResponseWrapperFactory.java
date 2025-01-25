package byzzbench.simulator.protocols.Zyzzyva.mutators;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.Zyzzyva.message.OrderedRequestMessage;
import byzzbench.simulator.protocols.Zyzzyva.message.SpeculativeResponse;
import byzzbench.simulator.protocols.Zyzzyva.message.SpeculativeResponseWrapper;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
public class SpeculativeResponseWrapperFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                // Small-scope mutations
                new MessageMutationFault("zyzzyva-speculative-response-request-seq-inc", "Increment Sequence Number", List.of(SpeculativeResponseWrapper.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof SpeculativeResponseWrapper message)) {
                            throw invalidMessageTypeException;
                        }

                        SpeculativeResponse sr = message.getSpecResponse();
                        SpeculativeResponse mutatedSr = sr.withSequenceNumber(sr.getSequenceNumber() + 1);
                        mutatedSr.sign(sr.getSignedBy());
                        SpeculativeResponseWrapper mutatedMessage = message.withSpecResponse(mutatedSr);
                        messageEvent.setPayload(mutatedMessage);
                    }
                }, new MessageMutationFault("zyzzyva-speculative-response-request-seq-dec", "Decrement Sequence Number", List.of(SpeculativeResponseWrapper.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof SpeculativeResponseWrapper message)) {
                            throw invalidMessageTypeException;
                        }

                        SpeculativeResponse sr = message.getSpecResponse();
                        SpeculativeResponse mutatedSr = sr.withSequenceNumber(sr.getSequenceNumber() - 1);
                        mutatedSr.sign(sr.getSignedBy());
                        SpeculativeResponseWrapper mutatedMessage = message.withSpecResponse(mutatedSr);
                        messageEvent.setPayload(mutatedMessage);
                    }
                }, new MessageMutationFault("zyzzyva-speculative-response-request-view-inc", "Increment View Number", List.of(SpeculativeResponseWrapper.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof SpeculativeResponseWrapper message)) {
                            throw invalidMessageTypeException;
                        }

                        SpeculativeResponse sr = message.getSpecResponse();
                        SpeculativeResponse mutatedSr = sr.withViewNumber(sr.getViewNumber() + 1);
                        mutatedSr.sign(sr.getSignedBy());
                        SpeculativeResponseWrapper mutatedMessage = message.withSpecResponse(mutatedSr);
                        messageEvent.setPayload(mutatedMessage);
                    }
                }, new MessageMutationFault("zyzzyva-speculative-response-request-view-dec", "Decrement View Number", List.of(SpeculativeResponseWrapper.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof SpeculativeResponseWrapper message)) {
                            throw invalidMessageTypeException;
                        }

                        SpeculativeResponse sr = message.getSpecResponse();
                        SpeculativeResponse mutatedSr = sr.withViewNumber(sr.getViewNumber() - 1);
                        mutatedSr.sign(sr.getSignedBy());
                        SpeculativeResponseWrapper mutatedMessage = message.withSpecResponse(mutatedSr);
                        messageEvent.setPayload(mutatedMessage);
                    }
                }, new MessageMutationFault("zyzzyva-speculative-response-prev-ordered-history", "Previous Ordered request history", List.of(SpeculativeResponseWrapper.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof SpeculativeResponseWrapper message)) {
                            throw invalidMessageTypeException;
                        }

                        OrderedRequestMessage orm = message.getOrderedRequest();
                        // previous history
                        OrderedRequestMessage mutatedOrm = orm.withHistory(orm.getHistory() ^ Arrays.hashCode(orm.getDigest()));
                        mutatedOrm.sign(orm.getSignedBy());
                        SpeculativeResponseWrapper mutatedMessage = message.withOrderedRequest(mutatedOrm);
                        messageEvent.setPayload(mutatedMessage);
                    }
                }, new MessageMutationFault("zyzzyva-speculative-response-inc-reply", "Increment reply", List.of(SpeculativeResponseWrapper.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof SpeculativeResponseWrapper message)) {
                            throw invalidMessageTypeException;
                        }

                        String response = message.getReply().toString();
                        String mutatedResponse = response + "1";
                        SpeculativeResponseWrapper mutatedMessage = message.withReply(mutatedResponse);
                        messageEvent.setPayload(mutatedMessage);
                    }
                }

//                // Any-scope mutations
//                , new MessageMutationFault("zyzzyva-speculative-response-request-seq-inc", "Increment Sequence Number", List.of(SpeculativeResponseWrapper.class)) {
//                    @Override
//                    public void accept(FaultContext serializable) {
//                        Optional<Event> event = serializable.getEvent();
//                        if (event.isEmpty()) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(event.get() instanceof MessageEvent messageEvent)) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(messageEvent.getPayload() instanceof SpeculativeResponseWrapper message)) {
//                            throw invalidMessageTypeException;
//                        }
//                        Random random = new Random();
//                        long randomSeq = random.nextLong(1, Long.MAX_VALUE);
//                        SpeculativeResponse sr = message.getSpecResponse();
//                        SpeculativeResponse mutatedSr = sr.withSequenceNumber(sr.getSequenceNumber() + randomSeq);
//                        mutatedSr.sign(sr.getSignedBy());
//                        SpeculativeResponseWrapper mutatedMessage = message.withSpecResponse(mutatedSr);
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                }, new MessageMutationFault("zyzzyva-speculative-response-request-seq-dec", "Decrement Sequence Number", List.of(SpeculativeResponseWrapper.class)) {
//                    @Override
//                    public void accept(FaultContext serializable) {
//                        Optional<Event> event = serializable.getEvent();
//                        if (event.isEmpty()) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(event.get() instanceof MessageEvent messageEvent)) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(messageEvent.getPayload() instanceof SpeculativeResponseWrapper message)) {
//                            throw invalidMessageTypeException;
//                        }
//
//                        Random random = new Random();
//                        long randomSeq = random.nextLong(1, Long.MAX_VALUE);
//                        SpeculativeResponse sr = message.getSpecResponse();
//                        SpeculativeResponse mutatedSr = sr.withSequenceNumber(sr.getSequenceNumber() - randomSeq);
//                        mutatedSr.sign(sr.getSignedBy());
//                        SpeculativeResponseWrapper mutatedMessage = message.withSpecResponse(mutatedSr);
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                }, new MessageMutationFault("zyzzyva-speculative-response-request-view-inc", "Increment View Number", List.of(SpeculativeResponseWrapper.class)) {
//                    @Override
//                    public void accept(FaultContext serializable) {
//                        Optional<Event> event = serializable.getEvent();
//                        if (event.isEmpty()) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(event.get() instanceof MessageEvent messageEvent)) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(messageEvent.getPayload() instanceof SpeculativeResponseWrapper message)) {
//                            throw invalidMessageTypeException;
//                        }
//                        Random random = new Random();
//                        long randomSeq = random.nextLong(1, Long.MAX_VALUE);
//                        SpeculativeResponse sr = message.getSpecResponse();
//                        SpeculativeResponse mutatedSr = sr.withViewNumber(sr.getViewNumber() + randomSeq);
//                        mutatedSr.sign(sr.getSignedBy());
//                        SpeculativeResponseWrapper mutatedMessage = message.withSpecResponse(mutatedSr);
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                }, new MessageMutationFault("zyzzyva-speculative-response-request-view-dec", "Decrement View Number", List.of(SpeculativeResponseWrapper.class)) {
//                    @Override
//                    public void accept(FaultContext serializable) {
//                        Optional<Event> event = serializable.getEvent();
//                        if (event.isEmpty()) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(event.get() instanceof MessageEvent messageEvent)) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(messageEvent.getPayload() instanceof SpeculativeResponseWrapper message)) {
//                            throw invalidMessageTypeException;
//                        }
//                        Random random = new Random();
//                        long randomSeq = random.nextLong(1, Long.MAX_VALUE);
//                        SpeculativeResponse sr = message.getSpecResponse();
//                        SpeculativeResponse mutatedSr = sr.withViewNumber(sr.getViewNumber() - randomSeq);
//                        mutatedSr.sign(sr.getSignedBy());
//                        SpeculativeResponseWrapper mutatedMessage = message.withSpecResponse(mutatedSr);
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                }
        );
    }
}

