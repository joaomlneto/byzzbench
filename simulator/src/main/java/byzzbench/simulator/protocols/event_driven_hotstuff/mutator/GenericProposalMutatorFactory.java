package byzzbench.simulator.protocols.event_driven_hotstuff.mutator;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.factories.MessageMutatorFactory;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.basic_hotstuff.QuorumCertificate;
import byzzbench.simulator.protocols.event_driven_hotstuff.EDHSNode;
import byzzbench.simulator.protocols.event_driven_hotstuff.EDHSQuorumCertificate;
import byzzbench.simulator.protocols.event_driven_hotstuff.EDHotStuffReplica;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.GenericMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Component
@ToString
public class GenericProposalMutatorFactory  extends MessageMutatorFactory  {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    @Override
    public List<MessageMutationFault> mutators() {
        return List.of(
                new MessageMutationFault(
                        "edhotstuff-generic-view-inc",
                        "Generic-Proposal: Increment View Number",
                        List.of(GenericMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
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

                        System.out.println("MUTATION: incremented GENERIC message (" + messageEvent.getSenderId() + " -> " + messageEvent.getRecipientId() + ") view from " + message.getViewNumber() + " to " + mutatedMessage.getViewNumber());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault(
                        "edhotstuff-generic-previous-qc",
                        "Generic-Proposal: Increment View Number",
                        List.of(GenericMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
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
                        EDHotStuffReplica senderReplica = (EDHotStuffReplica) serializable.getScenario().getNodes().get(messageEvent.getSenderId());
                        EDHSNode qcNode = senderReplica.getNode(originalNode.getJustify().getNodeHash());
                        EDHSQuorumCertificate replacementQC;
                        if(qcNode != null && !qcNode.getClientRequest().getRequestId().equals("GENESIS")) {
                            replacementQC = qcNode.getJustify();
                            System.out.println("MUTATION: replaced QC with previous on GENERIC message (" + messageEvent.getSenderId() + " -> " + messageEvent.getRecipientId() + ")");
                        }
                        else replacementQC = originalNode.getJustify();

                        EDHSNode newNode = null;
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
                        "edhotstuff-generic-view-dec",
                        "Generic-Proposal: Increment View Number",
                        List.of(GenericMessage.class)) {
                    @Override
                    public void accept(FaultContext serializable) {
                        Optional<Event> event = serializable.getEvent();
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
                            newNode = new EDHSNode(originalNode.getParentHash(), originalNode.getClientRequest(), originalNode.getJustify(), originalNode.getHeight() - 1);
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                        GenericMessage mutatedMessage = new GenericMessage(message.getViewNumber() - 1, newNode);
                        mutatedMessage.sign(message.getSignedBy());

                        System.out.println("MUTATION: incremented GENERIC message (" + messageEvent.getSenderId() + " -> " + messageEvent.getRecipientId() + ") view from " + message.getViewNumber() + " to " + mutatedMessage.getViewNumber());
                        messageEvent.setPayload(mutatedMessage);
                    }
                }
        );
    }
}
