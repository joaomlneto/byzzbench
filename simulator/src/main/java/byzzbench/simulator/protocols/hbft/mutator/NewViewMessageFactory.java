package byzzbench.simulator.protocols.hbft.mutator;

import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import lombok.ToString;

@Component
@ToString
public class NewViewMessageFactory extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");
    Random random = new Random();
    int bound = 100;

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                // new MessageMutationFault("hbft-new-view-view-inc", "Increment View Number",List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         NewViewMessage mutatedMessage = message.withNewViewNumber(message.getNewViewNumber() + 1);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-view-dec", "Decrement View Number", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         NewViewMessage mutatedMessage = message.withNewViewNumber(message.getNewViewNumber() - 1);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-checkpoint-decrement-seqNum", "Decrement checkpoint seqNum", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Checkpoint checkpoint = message.getCheckpoint();
                //         checkpoint.setSequenceNumber(checkpoint.getSequenceNumber() - 1);
                //         checkpoint.setHistory(checkpoint.getHistory().getHistoryBefore(checkpoint.getSequenceNumber() - 1));
                //         NewViewMessage mutatedMessage = message.withCheckpoint(checkpoint);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-checkpoint-increment-seqNum", "Increment checkpoint seqNum", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Checkpoint checkpoint = message.getCheckpoint();
                //         checkpoint.setSequenceNumber(checkpoint.getSequenceNumber() + 1);
                //         NewViewMessage mutatedMessage = message.withCheckpoint(checkpoint);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-remove-last-req-checkpoint", "Remove last request from checkpoint", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Checkpoint checkpoint = message.getCheckpoint();
                //         checkpoint.getHistory().getRequests().remove(checkpoint.getHistory().getRequests().lastEntry().getKey());
                //         NewViewMessage mutatedMessage = message.withCheckpoint(checkpoint);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-remove-first-req-checkpoint", "Remove first request from checkpoint", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Checkpoint checkpoint = message.getCheckpoint();
                //         checkpoint.getHistory().getRequests().remove(checkpoint.getHistory().getRequests().firstEntry().getKey());
                //         NewViewMessage mutatedMessage = message.withCheckpoint(checkpoint);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-remove-first-view-change-proof", "Remove first view change proof", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Collection<ViewChangeMessage> viewChanges = message.getViewChangeProofs();
                //         viewChanges.remove(viewChanges.iterator().next());
                //         NewViewMessage mutatedMessage = message.withViewChangeProofs(viewChanges);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-change-a-view-change-proof", "Change a view change proof (remove and add a new one)", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Collection<ViewChangeMessage> viewChanges = message.getViewChangeProofs();
                //         viewChanges.remove(viewChanges.iterator().next());
                //         ViewChangeMessage messageToCopy = viewChanges.iterator().next();
                //         ViewChangeMessage newViewChangeMessage = new ViewChangeMessage(messageToCopy.getNewViewNumber(),
                //             messageToCopy.getSpeculativeHistoryP(),
                //             messageToCopy.getSpeculativeHistoryQ(),
                //             messageToCopy.getRequestsR(),
                //             messageToCopy.getReplicaId());
                //         viewChanges.add(newViewChangeMessage);
                //         NewViewMessage mutatedMessage = message.withViewChangeProofs(viewChanges);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-add-increment-last-request", "Increment last req seq number", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         Entry<Long, RequestMessage> lastReq = requests.getRequests().lastEntry();
                //         requests.getRequests().remove(lastReq.getKey());
                //         requests.getRequests().put(lastReq.getKey() + 1, lastReq.getValue());
                //         NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-add-decrement-last-request", "Decrement last req seq number", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         Entry<Long, RequestMessage> lastReq = requests.getRequests().lastEntry();
                //         requests.getRequests().remove(lastReq.getKey());
                //         requests.getRequests().put(lastReq.getKey() - 1, lastReq.getValue());
                //         NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-add-increment-first-request", "Increment first req seq number", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         Entry<Long, RequestMessage> firstReq = requests.getRequests().firstEntry();
                //         requests.getRequests().remove(firstReq.getKey());
                //         requests.getRequests().put(firstReq.getKey() + 1, firstReq.getValue());
                //         NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-add-decrement-first-request", "Decrement first req seq number", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         Entry<Long, RequestMessage> firstReq = requests.getRequests().firstEntry();
                //         requests.getRequests().remove(firstReq.getKey());
                //         requests.getRequests().put(firstReq.getKey() - 1, firstReq.getValue());
                //         NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-remove-last-request", "Remove last request from R", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         requests.getRequests().remove(requests.getRequests().lastEntry().getKey());
                //         NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-remove-first-request", "Remove first request from R", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         requests.getRequests().remove(requests.getRequests().firstEntry().getKey());
                //         NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },

                // new MessageMutationFault("hbft-new-view-remove-change-last-req-null", "Change last request to null in R", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         requests.getRequests().lastEntry().setValue(null);
                //         NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-remove-change-first-req-null", "Change first request to null in R", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         requests.getRequests().firstEntry().setValue(null);
                //         NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-add-null-request", "Add null request to R", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         requests.addEntry(requests.getGreatestSeqNumber() + 1, null);
                //         NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // }


                // ANY-SCOPE mutations
                //,
                // new MessageMutationFault("hbft-new-view-view-inc", "Increment View Number",List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         NewViewMessage mutatedMessage = message.withNewViewNumber(message.getNewViewNumber() + 1);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-view-dec", "Decrement View Number", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         NewViewMessage mutatedMessage = message.withNewViewNumber(message.getNewViewNumber() - 1);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-checkpoint-decrement-seqNum", "Decrement checkpoint seqNum", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Checkpoint checkpoint = message.getCheckpoint();
                //         if (checkpoint != null && checkpoint.getHistory() != null && !checkpoint.getHistory().isEmpty()) {
                //             checkpoint.setSequenceNumber(checkpoint.getSequenceNumber() - 1);
                //             checkpoint.setHistory(checkpoint.getHistory().getHistoryBefore(checkpoint.getSequenceNumber() - 1));
                //             NewViewMessage mutatedMessage = message.withCheckpoint(checkpoint);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-checkpoint-increment-seqNum", "Increment checkpoint seqNum", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Checkpoint checkpoint = message.getCheckpoint();
                //         if (checkpoint != null) {
                //             checkpoint.setSequenceNumber(checkpoint.getSequenceNumber() + 1);
                //             NewViewMessage mutatedMessage = message.withCheckpoint(checkpoint);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-remove-last-req-checkpoint", "Remove last request from checkpoint", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Checkpoint checkpoint = message.getCheckpoint();
                //         if (checkpoint != null) {
                //             SpeculativeHistory history = checkpoint.getHistory();
                //             if (history != null && !history.getRequests().isEmpty()) {
                //                 long key = history.getRequests().lastKey();
                //                 history.getRequests().remove(key);
                //                 NewViewMessage mutatedMessage = message.withCheckpoint(checkpoint);
                //                 mutatedMessage.sign(message.getSignedBy());
                //                 messageEvent.setPayload(mutatedMessage);
                //             }
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-remove-first-req-checkpoint", "Remove first request from checkpoint", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Checkpoint checkpoint = message.getCheckpoint();
                //         if (checkpoint != null) {
                //             SpeculativeHistory history = checkpoint.getHistory();
                //             if (history != null && !history.getRequests().isEmpty()) {
                //                 long key = history.getRequests().firstKey();
                //                 history.getRequests().remove(key);
                //                 NewViewMessage mutatedMessage = message.withCheckpoint(checkpoint);
                //                 mutatedMessage.sign(message.getSignedBy());
                //                 messageEvent.setPayload(mutatedMessage);
                //             }
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-remove-first-view-change-proof", "Remove first view change proof", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Collection<ViewChangeMessage> viewChanges = message.getViewChangeProofs();
                //         if (!viewChanges.isEmpty()) {
                //             ViewChangeMessage mess = viewChanges.iterator().next();
                //             viewChanges.remove(mess);
                //             NewViewMessage mutatedMessage = message.withViewChangeProofs(viewChanges);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                        
                //     }
                // },
                // // new MessageMutationFault("hbft-new-view-change-a-view-change-proof", "Change a view change proof (remove and add a new one)", List.of(NewViewMessage.class)) {
                // //     @Override
                // //     public void accept(FaultContext serializable) {
                // //         Optional<Event> event = serializable.getEvent();
                // //         if (event.isEmpty()) {
                // //             throw invalidMessageTypeException;
                // //         }
                // //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                // //             throw invalidMessageTypeException;
                // //         }
                // //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                // //             throw invalidMessageTypeException;
                // //         }
                // //         Collection<ViewChangeMessage> viewChanges = message.getViewChangeProofs();
                // //         if (!viewChanges.isEmpty()) {
                // //             ViewChangeMessage mess = viewChanges.iterator().next();
                // //             viewChanges.remove(mess);
                // //             ViewChangeMessage messageToCopy = viewChanges.iterator().next();
                // //             ViewChangeMessage newViewChangeMessage = new ViewChangeMessage(messageToCopy.getNewViewNumber(),
                // //                 messageToCopy.getSpeculativeHistoryP(),
                // //                 messageToCopy.getSpeculativeHistoryQ(),
                // //                 messageToCopy.getRequestsR(),
                // //                 messageToCopy.getReplicaId());
                // //             viewChanges.add(newViewChangeMessage);
                // //             NewViewMessage mutatedMessage = message.withViewChangeProofs(viewChanges);
                // //             mutatedMessage.sign(message.getSignedBy());
                // //             messageEvent.setPayload(mutatedMessage);
                // //         }
                // //     }
                // // },
                // new MessageMutationFault("hbft-new-view-add-increment-last-request", "Increment last req seq number", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         if (requests != null && !requests.isEmpty()) {
                //             Entry<Long, RequestMessage> lastReq = requests.getRequests().lastEntry();
                //             requests.getRequests().remove(lastReq.getKey());
                //             requests.getRequests().put(lastReq.getKey() + 1, lastReq.getValue());
                //             NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-add-decrement-last-request", "Decrement last req seq number", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         if (requests != null && !requests.isEmpty()) {
                //             Entry<Long, RequestMessage> lastReq = requests.getRequests().lastEntry();
                //             requests.getRequests().remove(lastReq.getKey());
                //             requests.getRequests().put(lastReq.getKey() - 1, lastReq.getValue());
                //             NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-add-increment-first-request", "Increment first req seq number", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         if (requests != null && !requests.isEmpty()) {
                //             Entry<Long, RequestMessage> firstReq = requests.getRequests().firstEntry();
                //             requests.getRequests().remove(firstReq.getKey());
                //             requests.getRequests().put(firstReq.getKey() + 1, firstReq.getValue());
                //             NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-add-decrement-first-request", "Decrement first req seq number", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         if (requests != null && !requests.isEmpty()) {
                //             Entry<Long, RequestMessage> firstReq = requests.getRequests().firstEntry();
                //             requests.getRequests().remove(firstReq.getKey());
                //             requests.getRequests().put(firstReq.getKey() - 1, firstReq.getValue());
                //             NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-remove-last-request", "Remove last request from R", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         if (requests != null && !requests.isEmpty() && !requests.getRequests().isEmpty()) {
                //             long key = requests.getRequests().lastEntry().getKey();
                //             requests.getRequests().remove(key);
                //             NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-remove-first-request", "Remove first request from R", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         if (requests != null && !requests.isEmpty() && !requests.getRequests().isEmpty()) {
                //             long key = requests.getRequests().firstKey();
                //             requests.getRequests().remove(key);
                //             NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-remove-change-last-req-null", "Change last request to null in R", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         if (requests != null && !requests.isEmpty() && !requests.getRequests().isEmpty()) {
                //             long key = requests.getRequests().lastKey();
                //             requests.getRequests().put(key, null);
                //             NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-remove-change-first-req-null", "Change first request to null in R", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         if (requests != null && !requests.isEmpty() && !requests.getRequests().isEmpty()) {
                //             long key = requests.getRequests().firstKey();
                //             requests.getRequests().put(key, null);
                //             NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-add-null-request", "Add null request to R", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         requests.addEntry(requests.getGreatestSeqNumber() + 1, null);
                //         NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // }


                // ANY-SCOPE mutations
                //,
                // new MessageMutationFault("hbft-new-view-view-inc", "Increment View Number",List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         NewViewMessage mutatedMessage = message.withNewViewNumber(message.getNewViewNumber() + random.nextLong(bound));
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-view-dec", "Decrement View Number", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         NewViewMessage mutatedMessage = message.withNewViewNumber(message.getNewViewNumber() - random.nextLong(bound));
                //         mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-checkpoint-decrement-seqNum", "Decrement checkpoint seqNum", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Checkpoint checkpoint = message.getCheckpoint();
                //         if (checkpoint != null && checkpoint.getHistory() != null && !checkpoint.getHistory().isEmpty()) {
                //             Long randomLong = random.nextLong(bound);
                //             checkpoint.setSequenceNumber(checkpoint.getSequenceNumber() - randomLong);
                //             checkpoint.setHistory(checkpoint.getHistory().getHistoryBefore(checkpoint.getSequenceNumber() - randomLong));
                //             NewViewMessage mutatedMessage = message.withCheckpoint(checkpoint);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-checkpoint-increment-seqNum", "Increment checkpoint seqNum", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Checkpoint checkpoint = message.getCheckpoint();
                //         if (checkpoint != null && checkpoint.getHistory() != null && !checkpoint.getHistory().isEmpty()) {
                //             checkpoint.setSequenceNumber(checkpoint.getSequenceNumber() + random.nextLong(bound));
                //             NewViewMessage mutatedMessage = message.withCheckpoint(checkpoint);
                //             mutatedMessage.sign(message.getSignedBy());
                //         messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-remove-random-req-checkpoint", "Remove random request from checkpoint", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         Checkpoint checkpoint = message.getCheckpoint();
                //         if (checkpoint != null && checkpoint.getHistory() != null && !checkpoint.getHistory().isEmpty()) {
                //             List<Long> keysetAsArray = new ArrayList<Long>(checkpoint.getHistory().getRequests().keySet());
                //             checkpoint.getHistory().getRequests().remove(keysetAsArray.get(random.nextInt(keysetAsArray.size())));
                //             NewViewMessage mutatedMessage = message.withCheckpoint(checkpoint);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-add-increment-random-request", "Increment random req seq number", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         if (requests != null && !requests.isEmpty()) {
                //             List<Long> keysetAsArray = new ArrayList<Long>(requests.getRequests().keySet());
                //             Long reqKey = keysetAsArray.get(random.nextInt(keysetAsArray.size()));
                //             RequestMessage reqValue = requests.getRequests().get(reqKey);
                //             requests.getRequests().remove(reqKey);
                //             requests.getRequests().put(reqKey + random.nextLong(bound), reqValue);
                //             NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-add-decrement-random-request", "Decrement random req seq number", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         if (requests != null && !requests.isEmpty()) {
                //             List<Long> keysetAsArray = new ArrayList<Long>(requests.getRequests().keySet());
                //             Long reqKey = keysetAsArray.get(random.nextInt(keysetAsArray.size()));
                //             RequestMessage reqValue = requests.getRequests().get(reqKey);
                //             requests.getRequests().remove(reqKey);
                //             requests.getRequests().put(reqKey - random.nextLong(bound), reqValue);
                //             NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-remove-random-request", "Remove random request from R", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         if (requests != null && !requests.isEmpty()) {
                //             List<Long> keysetAsArray = new ArrayList<Long>(requests.getRequests().keySet());
                //             requests.getRequests().remove(keysetAsArray.get(random.nextInt(keysetAsArray.size())));
                //             NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // },
                // new MessageMutationFault("hbft-new-view-remove-change-random-req-null", "Change random request to null in R", List.of(NewViewMessage.class)) {
                //     @Override
                //     public void accept(FaultContext serializable) {
                //         Optional<Event> event = serializable.getEvent();
                //         if (event.isEmpty()) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(event.get() instanceof MessageEvent messageEvent)) {
                //             throw invalidMessageTypeException;
                //         }
                //         if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                //             throw invalidMessageTypeException;
                //         }
                //         SpeculativeHistory requests = message.getSpeculativeHistory();
                //         if (requests != null && !requests.isEmpty()) {
                //             List<Long> keysetAsArray = new ArrayList<Long>(requests.getRequests().keySet());
                //             requests.getRequests().put(keysetAsArray.get(random.nextInt(keysetAsArray.size())), null);
                //             NewViewMessage mutatedMessage = message.withSpeculativeHistory(requests);
                //             mutatedMessage.sign(message.getSignedBy());
                //             messageEvent.setPayload(mutatedMessage);
                //         }
                //     }
                // }

        );
    }
}
