package byzzbench.simulator.protocols.Zyzzyva.mutators;

import byzzbench.simulator.Node;
import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.Zyzzyva.CommitCertificate;
import byzzbench.simulator.protocols.Zyzzyva.ZyzzyvaReplica;
import byzzbench.simulator.protocols.Zyzzyva.message.*;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ViewChangeMessageWrapperMutator extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    public SortedMap<Long, OrderedRequestMessageWrapper> swapOrderedRequestHistory(SortedMap<Long, OrderedRequestMessageWrapper> orderedRequestHistory) {
        SortedMap<Long, OrderedRequestMessageWrapper> mutatedOrderedRequestHistory = new TreeMap<>(orderedRequestHistory);
        if (orderedRequestHistory.isEmpty() || orderedRequestHistory.size() == 1) {
            return orderedRequestHistory;
        }
        OrderedRequestMessageWrapper firstOrmw = orderedRequestHistory.get(orderedRequestHistory.firstKey());
        OrderedRequestMessageWrapper lastOrmw = orderedRequestHistory.get(orderedRequestHistory.lastKey());
        OrderedRequestMessage first = firstOrmw.getOrderedRequest();
        OrderedRequestMessage last = lastOrmw.getOrderedRequest();
        OrderedRequestMessageWrapper firstMutatedOrmw =  lastOrmw.withOrderedRequest(last.withHistory(Arrays.hashCode(last.getDigest())));
        firstMutatedOrmw = firstMutatedOrmw.withOrderedRequest(firstMutatedOrmw.getOrderedRequest().withSequenceNumber(first.getSequenceNumber()));
        long calculatedLastHistory = Arrays.hashCode(last.getDigest()) ^ Arrays.hashCode(first.getDigest());
        OrderedRequestMessageWrapper lastMutatedOrmw = firstOrmw.withOrderedRequest(first.withHistory(calculatedLastHistory));
        lastMutatedOrmw = lastMutatedOrmw.withOrderedRequest(lastMutatedOrmw.getOrderedRequest().withSequenceNumber(last.getSequenceNumber()));
        mutatedOrderedRequestHistory.put(orderedRequestHistory.firstKey(), firstMutatedOrmw);
        mutatedOrderedRequestHistory.put(orderedRequestHistory.lastKey(), lastMutatedOrmw);
        return mutatedOrderedRequestHistory;
    }

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                // small-scope mutations
                new MessageMutationFault("zyzzyva-view-change-message-view-number-inc", "View change message increment view number", List.of(ViewChangeMessageWrapper.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewChangeMessageWrapper message)) {
                            throw invalidMessageTypeException;
                        }

                        ViewChangeMessage vcm = message.getViewChangeMessage();
                        ViewChangeMessage mutatedVcm = vcm.withFutureViewNumber(vcm.getFutureViewNumber() + 1);
                        mutatedVcm.sign(vcm.getSignedBy());
                        ViewChangeMessageWrapper mutatedMessage = message.withViewChangeMessage(mutatedVcm);
                        messageEvent.setPayload(mutatedMessage);
                    }
                }, new MessageMutationFault("zyzzyva-view-change-message-view-number-dec", "View change message decrement view number", List.of(ViewChangeMessageWrapper.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewChangeMessageWrapper message)) {
                            throw invalidMessageTypeException;
                        }

                        ViewChangeMessage vcm = message.getViewChangeMessage();
                        ViewChangeMessage mutatedVcm = vcm.withFutureViewNumber(vcm.getFutureViewNumber() - 1);
                        mutatedVcm.sign(vcm.getSignedBy());
                        ViewChangeMessageWrapper mutatedMessage = message.withViewChangeMessage(mutatedVcm);
                        messageEvent.setPayload(mutatedMessage);
                    }
                }, new MessageMutationFault("zyzzyva-view-change-message-prev-commit-certificate", "View Change Message Previous Commit Certificate", List.of(ViewChangeMessageWrapper.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewChangeMessageWrapper message)) {
                            throw invalidMessageTypeException;
                        }

                        Node sender = serializable.getScenario().getNode(messageEvent.getSenderId());
                        if (!(sender instanceof ZyzzyvaReplica zyzzyvaReplica)) {
                            throw invalidMessageTypeException;
                        }
                        SequencedSet<Long> keySet = zyzzyvaReplica.getMessageLog().getCommitCertificates().sequencedKeySet().reversed();
                        if (keySet.isEmpty() || keySet.size() == 1) {
                            return;
                        }
                        keySet.removeFirst();
                        long prevKey = keySet.getFirst();
                        CommitCertificate prev = zyzzyvaReplica.getMessageLog().getCommitCertificates().get(prevKey);

                        ViewChangeMessage vcm = message.getViewChangeMessage();
                        ViewChangeMessage mutatedVcm = vcm.withCommitCertificate(prev);
                        mutatedVcm.sign(vcm.getSignedBy());
                        ViewChangeMessageWrapper mutatedMessage = message.withViewChangeMessage(mutatedVcm);
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
//                // DO NOT COMMENT THIS ONE OUT
//
                new MessageMutationFault("zyzzyva-view-change-message-swap-ordered-request-history", "View change message swap ordered request history", List.of(ViewChangeMessageWrapper.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof ViewChangeMessageWrapper message)) {
                            throw invalidMessageTypeException;
                        }
                        ViewChangeMessage vcm = message.getViewChangeMessage();
                        SortedMap<Long, OrderedRequestMessageWrapper> mutatedOrderedRequestHistory = swapOrderedRequestHistory(vcm.getOrderedRequestHistory());
                        ViewChangeMessage mutatedVcm = vcm.withOrderedRequestHistory(mutatedOrderedRequestHistory);
                        mutatedVcm.sign(vcm.getSignedBy());
                        ViewChangeMessageWrapper mutatedMessage = message.withViewChangeMessage(mutatedVcm);
                        messageEvent.setPayload(mutatedMessage);
                    }
                }
// Any-scope mutations
//                ,
//                new MessageMutationFault("zyzzyva-view-change-message-random-commit-certificate", "View Change Message Random Commit Certificate", List.of(ViewChangeMessageWrapper.class)) {
//                    @Override
//                    public void accept(FaultContext serializable) {
//                        Optional<Event> event = serializable.getEvent();
//                        if (event.isEmpty()) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(event.get() instanceof MessageEvent messageEvent)) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(messageEvent.getPayload() instanceof ViewChangeMessageWrapper message)) {
//                            throw invalidMessageTypeException;
//                        }
//
//                        Node sender = serializable.getScenario().getNode(messageEvent.getSenderId());
//                        if (!(sender instanceof ZyzzyvaReplica zyzzyvaReplica)) {
//                            throw invalidMessageTypeException;
//                        }
//                        List<Long> keySet = new ArrayList<>(zyzzyvaReplica.getMessageLog().getCommitCertificates().sequencedKeySet().reversed().stream().toList());
//                        if (keySet.isEmpty() || keySet.size() == 1) {
//                            return;
//                        }
//                        Collections.shuffle(keySet);
//                        long prevKey = keySet.getFirst();
//                        CommitCertificate prev = zyzzyvaReplica.getMessageLog().getCommitCertificates().get(prevKey);
//                        ViewChangeMessage vcm = message.getViewChangeMessage();
//                        ViewChangeMessage mutatedVcm = vcm.withCommitCertificate(prev);
//                        mutatedVcm.sign(vcm.getSignedBy());
//                        ViewChangeMessageWrapper mutatedMessage = message.withViewChangeMessage(mutatedVcm);
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                }
//
//                , new MessageMutationFault("zyzzyva-view-change-message-view-number-dec-any", "View change message decrement view number Any", List.of(ViewChangeMessageWrapper.class)) {
//                    @Override
//                    public void accept(FaultContext serializable) {
//                        Optional<Event> event = serializable.getEvent();
//                        if (event.isEmpty()) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(event.get() instanceof MessageEvent messageEvent)) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(messageEvent.getPayload() instanceof ViewChangeMessageWrapper message)) {
//                            throw invalidMessageTypeException;
//                        }
//                        Random random = new Random();
//                        long randomView = random.nextLong(0, Long.MAX_VALUE);
//                        ViewChangeMessage vcm = message.getViewChangeMessage();
//                        ViewChangeMessage mutatedVcm = vcm.withFutureViewNumber(vcm.getFutureViewNumber() - randomView);
//                        mutatedVcm.sign(vcm.getSignedBy());
//                        ViewChangeMessageWrapper mutatedMessage = message.withViewChangeMessage(mutatedVcm);
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                }, new MessageMutationFault("zyzzyva-view-change-message-view-number-inc-any", "View change message increment view number any", List.of(ViewChangeMessageWrapper.class)) {
//                    @Override
//                    public void accept(FaultContext serializable) {
//                        Optional<Event> event = serializable.getEvent();
//                        if (event.isEmpty()) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(event.get() instanceof MessageEvent messageEvent)) {
//                            throw invalidMessageTypeException;
//                        }
//                        if (!(messageEvent.getPayload() instanceof ViewChangeMessageWrapper message)) {
//                            throw invalidMessageTypeException;
//                        }
//                        Random random = new Random();
//                        long randomView = random.nextLong(0, Long.MAX_VALUE);
//                        ViewChangeMessage vcm = message.getViewChangeMessage();
//                        ViewChangeMessage mutatedVcm = vcm.withFutureViewNumber(vcm.getFutureViewNumber() + randomView);
//                        mutatedVcm.sign(vcm.getSignedBy());
//                        ViewChangeMessageWrapper mutatedMessage = message.withViewChangeMessage(mutatedVcm);
//                        messageEvent.setPayload(mutatedMessage);
//                    }
//                }
        );
    }
}
