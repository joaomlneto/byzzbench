package byzzbench.simulator.protocols.event_driven_hotstuff.mutator.any_scope;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.event_driven_hotstuff.*;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.NewViewMessage;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;

import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class ASVoteMutator {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    public List<MessageMutationFault> getMutationFaults() {
        return List.of(
                new MessageMutationFault(
                        "edhotstuff-newview-rnd-view",
                        "New-View: Set Random View Number",
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

                        NewViewMessage mutatedMessage = new NewViewMessage((long) (Math.random() * 200), message.getJustify());
                        mutatedMessage.sign(message.getSignedBy());

                        if(faultContext.getScenario() instanceof EDHotStuffScenario scenario)
                            scenario.log("MUTATION: Changed NEW-VIEW message (" + messageEvent.getSenderId() + " -> " + messageEvent.getRecipientId() + ") view from " + message.getViewNumber() + " to " + mutatedMessage.getViewNumber());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault(
                        "edhotstuff-newview-rnd-qc",
                        "New-View: Set Random QC",
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
                        List<EDHSQuorumCertificate> availableQCs = senderReplica.getHashNodeMap().values().stream().filter(n -> !n.getClientRequest().getRequestId().equals("GENESIS")).map(n -> n.getJustify()).filter(qc -> !qc.equals(message.getJustify())).toList();
                        EDHSQuorumCertificate newQC = message.getJustify();
                        if(!availableQCs.isEmpty()) {
                            newQC = availableQCs.get((int) (Math.random() * availableQCs.size()));

                            if(faultContext.getScenario() instanceof EDHotStuffScenario scenario)
                                scenario.log("MUTATION: Changed NEW-VIEW message QC (" + messageEvent.getSenderId() + " -> " + messageEvent.getRecipientId() + ") from " + message.getJustify().getNodeHash() + " to " + newQC.getNodeHash());
                        }
                        NewViewMessage mutatedMessage = new NewViewMessage(message.getViewNumber(), newQC);
                        mutatedMessage.sign(message.getSignedBy());

                        messageEvent.setPayload(mutatedMessage);
                    }
                }
        );
    }
}
