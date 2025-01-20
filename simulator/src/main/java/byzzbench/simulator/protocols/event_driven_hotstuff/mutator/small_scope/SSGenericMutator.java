package byzzbench.simulator.protocols.event_driven_hotstuff.mutator.small_scope;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.event_driven_hotstuff.*;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.GenericMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;

import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class SSGenericMutator {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    public List<MessageMutationFault> getMutationFaults() {
        return List.of(
                new MessageMutationFault(
                        "edhotstuff-generic-view-inc",
                        "Generic-Proposal: Increment View Number",
                        List.of(GenericMessage.class)) {
                    @Override
                    public void accept(FaultContext faultContext) {
                        Optional<Event> event = faultContext.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof GenericMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        EDHSNode originalNode = message.getNode();
                        EDHSNode newNode = null;
                        try {
                            newNode = new EDHSNode(originalNode.getParentHash(), originalNode.getClientRequest(), originalNode.getJustify(), originalNode.getHeight() + 1);
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                        GenericMessage mutatedMessage = new GenericMessage(message.getViewNumber() + 1, newNode);
                        mutatedMessage.sign(message.getSignedBy());

                        if(faultContext.getScenario() instanceof EDHotStuffScenario scenario)
                            scenario.log("MUTATION: incremented GENERIC message (" + messageEvent.getSenderId() + " -> " + messageEvent.getRecipientId() + ") view from " + message.getViewNumber() + " to " + mutatedMessage.getViewNumber());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault(
                        "edhotstuff-generic-view-dec",
                        "Generic-Proposal: Decrement View Number",
                        List.of(GenericMessage.class)) {
                    @Override
                    public void accept(FaultContext faultContext) {
                        Optional<Event> event = faultContext.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof GenericMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        EDHSNode originalNode = message.getNode();
                        EDHSNode newNode;
                        try {
                            newNode = new EDHSNode(originalNode.getParentHash(), originalNode.getClientRequest(), originalNode.getJustify(), originalNode.getHeight() - 1);
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                        GenericMessage mutatedMessage = new GenericMessage(message.getViewNumber() - 1, newNode);
                        mutatedMessage.sign(message.getSignedBy());

                        if(faultContext.getScenario() instanceof EDHotStuffScenario scenario)
                            scenario.log("MUTATION: decremented GENERIC message (" + messageEvent.getSenderId() + " -> " + messageEvent.getRecipientId() + ") view from " + message.getViewNumber() + " to " + mutatedMessage.getViewNumber());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault(
                        "edhotstuff-generic-change-client-request-to-parent",
                        "Generic-Proposal: Change Client Request to Parent",
                        List.of(GenericMessage.class)) {
                    @Override
                    public void accept(FaultContext faultContext) {
                        Optional<Event> event = faultContext.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof GenericMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        if(!(faultContext.getScenario() instanceof EDHotStuffScenario edHotStuffScenario)) {
                            throw invalidMessageTypeException;
                        }
                        EDHSNode originalNode = message.getNode();
                        EDHotStuffReplica senderReplica = (EDHotStuffReplica) faultContext.getScenario().getNodes().get(messageEvent.getSenderId());
                        EDHSNode parentNode = senderReplica.getNode(originalNode.getParentHash());
                        EDHSNode newNode;
                        try {
                            newNode = new EDHSNode(originalNode.getParentHash(), parentNode.getClientRequest(), originalNode.getJustify(), originalNode.getHeight());
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                        GenericMessage mutatedMessage = new GenericMessage(message.getViewNumber(), newNode);
                        mutatedMessage.sign(message.getSignedBy());

                        if(faultContext.getScenario() instanceof EDHotStuffScenario scenario)
                            scenario.log("MUTATION: Changed request of GENERIC message (" + messageEvent.getSenderId() + " -> " + messageEvent.getRecipientId() + ") view from " + originalNode.getClientRequest() + " to " + newNode.getClientRequest());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault(
                        "edhotstuff-generic-previous-qc",
                        "Generic-Proposal: Replace with prev QC",
                        List.of(GenericMessage.class)) {
                    @Override
                    public void accept(FaultContext faultContext) {
                        Optional<Event> event = faultContext.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof GenericMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        EDHSNode originalNode = message.getNode();
                        EDHotStuffReplica senderReplica = (EDHotStuffReplica) faultContext.getScenario().getNodes().get(messageEvent.getSenderId());
                        EDHSNode qcNode = senderReplica.getNode(originalNode.getJustify().getNodeHash());
                        EDHSQuorumCertificate replacementQC;
                        if(qcNode != null && !qcNode.getClientRequest().getRequestId().equals("GENESIS")) {
                            replacementQC = qcNode.getJustify();
                            if(faultContext.getScenario() instanceof EDHotStuffScenario scenario)
                                scenario.log("MUTATION: replaced QC with previous on GENERIC message (" + messageEvent.getSenderId() + " -> " + messageEvent.getRecipientId() + ")");
                        }
                        else replacementQC = originalNode.getJustify();

                        EDHSNode newNode;
                        try {
                            newNode = new EDHSNode(originalNode.getParentHash(), originalNode.getClientRequest(), replacementQC, originalNode.getHeight());
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                        GenericMessage mutatedMessage = new GenericMessage(message.getViewNumber(), newNode);
                        mutatedMessage.sign(message.getSignedBy());


                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault(
                        "edhotstuff-generic-replace-parent-with-grandparent",
                        "Generic-Proposal: Replace the parent node with the grandparent node",
                        List.of(GenericMessage.class)) {
                    @Override
                    public void accept(FaultContext faultContext) {
                        Optional<Event> event = faultContext.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof GenericMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        EDHSNode originalNode = message.getNode();
                        EDHotStuffReplica senderReplica = (EDHotStuffReplica) faultContext.getScenario().getNodes().get(messageEvent.getSenderId());
                        EDHSNode parentNode = senderReplica.getNode(originalNode.getParentHash());
                        String replacementParentHash = originalNode.getParentHash();
                        if(parentNode != null && !parentNode.getClientRequest().getRequestId().equals("GENESIS")) {
                            replacementParentHash = parentNode.getParentHash();
                            if(faultContext.getScenario() instanceof EDHotStuffScenario scenario)
                                scenario.log("MUTATION: replaced parent with grandparent on GENERIC message (" + messageEvent.getSenderId() + " -> " + messageEvent.getRecipientId() + ")");
                        }

                        EDHSNode newNode;
                        try {
                            newNode = new EDHSNode(replacementParentHash, originalNode.getClientRequest(), originalNode.getJustify(), originalNode.getHeight());
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                        GenericMessage mutatedMessage = new GenericMessage(message.getViewNumber(), newNode);
                        mutatedMessage.sign(message.getSignedBy());


                        messageEvent.setPayload(mutatedMessage);
                    }

                },
                new MessageMutationFault(
                        "edhotstuff-generic-both-parent-and-qc",
                        "Generic-Proposal: Replace the parent node with the grandparent node",
                        List.of(GenericMessage.class)) {
                    @Override
                    public void accept(FaultContext faultContext) {
                        Optional<Event> event = faultContext.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof GenericMessage message)) {
                            throw invalidMessageTypeException;
                        }
                        EDHSNode originalNode = message.getNode();
                        EDHotStuffReplica senderReplica = (EDHotStuffReplica) faultContext.getScenario().getNodes().get(messageEvent.getSenderId());
                        EDHSNode parentNode = senderReplica.getNode(originalNode.getParentHash());
                        String replacementParentHash = originalNode.getParentHash();
                        EDHSNode qcNode = senderReplica.getNode(originalNode.getJustify().getNodeHash());
                        EDHSQuorumCertificate replacementQC = originalNode.getJustify();
                        if(qcNode != null && !qcNode.getClientRequest().getRequestId().equals("GENESIS") && parentNode != null && !parentNode.getClientRequest().getRequestId().equals("GENESIS")) {
                            replacementQC = qcNode.getJustify();
                            replacementParentHash = parentNode.getParentHash();
                            if(faultContext.getScenario() instanceof EDHotStuffScenario scenario)
                                scenario.log("MUTATION: replaced both parent and QC on GENERIC message (" + messageEvent.getSenderId() + " -> " + messageEvent.getRecipientId() + ")");
                        }

                        EDHSNode newNode;
                        try {
                            newNode = new EDHSNode(replacementParentHash, originalNode.getClientRequest(), replacementQC, originalNode.getHeight());
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                        GenericMessage mutatedMessage = new GenericMessage(message.getViewNumber(), newNode);
                        mutatedMessage.sign(message.getSignedBy());

                        messageEvent.setPayload(mutatedMessage);
                    }

                }
        );
    }
}
