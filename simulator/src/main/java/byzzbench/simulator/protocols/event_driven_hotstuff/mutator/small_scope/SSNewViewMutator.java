package byzzbench.simulator.protocols.event_driven_hotstuff.mutator.small_scope;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.event_driven_hotstuff.EDHSNode;
import byzzbench.simulator.protocols.event_driven_hotstuff.EDHSQuorumCertificate;
import byzzbench.simulator.protocols.event_driven_hotstuff.EDHotStuffReplica;
import byzzbench.simulator.protocols.event_driven_hotstuff.EDHotStuffScenario;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.NewViewMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;

import java.util.List;
import java.util.Optional;

public class SSNewViewMutator {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    public List<MessageMutationFault> getMutationFaults() {
        return List.of(
                new MessageMutationFault(
                        "edhotstuff-newview-view-inc",
                        "New-View: Increment View Number",
                        List.of(NewViewMessage.class)) {
                    @Override
                    public void accept(FaultContext faultContext) {
                        Optional<Event> event = faultContext.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                            throw invalidMessageTypeException;
                        }

                        NewViewMessage mutatedMessage = new NewViewMessage(message.getViewNumber() + 1, message.getJustify());
                        mutatedMessage.sign(message.getSignedBy());

                        if(faultContext.getScenario() instanceof EDHotStuffScenario scenario)
                            scenario.log("MUTATION: incremented NEW-VIEW message (" + messageEvent.getSenderId() + " -> " + messageEvent.getRecipientId() + ") view from " + message.getViewNumber() + " to " + mutatedMessage.getViewNumber());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault(
                        "edhotstuff-newview-view-dec",
                        "New-View: Decrement View Number",
                        List.of(NewViewMessage.class)) {
                    @Override
                    public void accept(FaultContext faultContext) {
                        Optional<Event> event = faultContext.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        NewViewMessage mutatedMessage = new NewViewMessage(message.getViewNumber() - 1, message.getJustify());
                        mutatedMessage.sign(message.getSignedBy());

                        if(faultContext.getScenario() instanceof EDHotStuffScenario scenario)
                            scenario.log("MUTATION: decremented NEW-VIEW message (" + messageEvent.getSenderId() + " -> " + messageEvent.getRecipientId() + ") view from " + message.getViewNumber() + " to " + mutatedMessage.getViewNumber());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault(
                        "edhotstuff-new-view-previous-qc",
                        "New-View: Replace with prev QC",
                        List.of(NewViewMessage.class)) {
                    @Override
                    public void accept(FaultContext faultContext) {
                        Optional<Event> event = faultContext.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof NewViewMessage message)) {
                            throw invalidMessageTypeException;
                        }

                        EDHotStuffReplica senderReplica = (EDHotStuffReplica) faultContext.getScenario().getNodes().get(messageEvent.getSenderId());
                        EDHSNode qcNode = senderReplica.getNode(message.getJustify().getNodeHash());
                        EDHSQuorumCertificate replacementQC;
                        if(qcNode != null && !qcNode.getClientRequest().getRequestId().equals("GENESIS")) {
                            replacementQC = qcNode.getJustify();
                            if(faultContext.getScenario() instanceof EDHotStuffScenario scenario)
                                scenario.log("MUTATION: replaced QC with previous on NEW-VIEW message (" + messageEvent.getSenderId() + " -> " + messageEvent.getRecipientId() + ")");
                        }
                        else replacementQC = message.getJustify();

                        NewViewMessage mutatedMessage = new NewViewMessage(message.getViewNumber(), replacementQC);
                        mutatedMessage.sign(message.getSignedBy());

                        messageEvent.setPayload(mutatedMessage);
                    }
                }
        );
    }
}
