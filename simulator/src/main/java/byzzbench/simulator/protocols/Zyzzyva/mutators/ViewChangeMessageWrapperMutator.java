package byzzbench.simulator.protocols.Zyzzyva.mutators;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.Zyzzyva.message.*;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;

import java.util.List;
import java.util.Optional;

public class ViewChangeMessageWrapperMutator extends MessageMutatorFactory {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault("zyzzyva-view-change-message-drop-ihtpms", "View change message drop IHateThePrimaries", List.of(ViewChangeMessageWrapper.class)) {
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

                        List<IHateThePrimaryMessage> iHateThePrimaries = message.getIHateThePrimaries();
                        if (iHateThePrimaries.size() >= 3)
                            iHateThePrimaries = iHateThePrimaries.subList(2, iHateThePrimaries.size());
                        ViewChangeMessageWrapper mutatedMessage = message.withIHateThePrimaries(iHateThePrimaries);
                        messageEvent.setPayload(message);
                    }
                }, new MessageMutationFault("zyzzyva-view-change-message-view-number-inc", "View change message increment view number", List.of(ViewChangeMessageWrapper.class)) {
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
                }, new MessageMutationFault("zyzzyva-view-change-message-checkpoint-inc", "View change message increment checkpoint", List.of(ViewChangeMessageWrapper.class)) {
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
                        ViewChangeMessage mutatedVcm = vcm.withStableCheckpoint(vcm.getStableCheckpoint() + 1);
                        mutatedVcm.sign(vcm.getSignedBy());
                        ViewChangeMessageWrapper mutatedMessage = message.withViewChangeMessage(mutatedVcm);
                        messageEvent.setPayload(mutatedMessage);
                    }
                }, new MessageMutationFault("zyzzyva-view-change-message-checkpoint-dec", "View change message decrement checkpoint", List.of(ViewChangeMessageWrapper.class)) {
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
                        ViewChangeMessage mutatedVcm = vcm.withStableCheckpoint(vcm.getStableCheckpoint() - 1);
                        mutatedVcm.sign(vcm.getSignedBy());
                        ViewChangeMessageWrapper mutatedMessage = message.withViewChangeMessage(mutatedVcm);
                        messageEvent.setPayload(mutatedMessage);
                    }
                }, new MessageMutationFault("zyzzyva-view-change-message-remove-checkpoint-message", "View change message remove checkpoint message", List.of(ViewChangeMessageWrapper.class)) {
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
                        List<CheckpointMessage> checkpoints = vcm.getCheckpoints();
                        if (checkpoints.size() >= 3)
                            checkpoints = checkpoints.subList(2, checkpoints.size());
                        ViewChangeMessage mutatedVcm = vcm.withCheckpoints(checkpoints);
                        mutatedVcm.sign(vcm.getSignedBy());
                        ViewChangeMessageWrapper mutatedMessage = message.withViewChangeMessage(mutatedVcm);
                        messageEvent.setPayload(mutatedMessage);
                    }
                }, new MessageMutationFault("zyzzyva-view-change-message-create-fake-checkpoint-message", "View change message create fake checkpoint message", List.of(ViewChangeMessageWrapper.class)) {
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
                        List<CheckpointMessage> checkpoints = vcm.getCheckpoints();
                        CheckpointMessage fakeCheckpoint = new CheckpointMessage(-1, -1, "fake");
                        checkpoints.add(fakeCheckpoint);
                        ViewChangeMessage mutatedVcm = vcm.withCheckpoints(checkpoints);
                        mutatedVcm.sign(vcm.getSignedBy());
                        ViewChangeMessageWrapper mutatedMessage = message.withViewChangeMessage(mutatedVcm);
                        messageEvent.setPayload(mutatedMessage);
                    }
                });
    }
}
