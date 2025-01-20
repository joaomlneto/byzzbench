package byzzbench.simulator.protocols.event_driven_hotstuff.mutator.any_scope;

import byzzbench.simulator.faults.FaultContext;
import byzzbench.simulator.faults.faults.MessageMutationFault;
import byzzbench.simulator.protocols.event_driven_hotstuff.*;
import byzzbench.simulator.protocols.event_driven_hotstuff.messages.GenericVote;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessageEvent;

import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class ASNewViewMutator {
    RuntimeException invalidMessageTypeException = new IllegalArgumentException("Invalid message type");

    public List<MessageMutationFault> getMutationFaults() {
        return List.of(
                new MessageMutationFault(
                        "edhotstuff-generic-vote-rnd-view",
                        "Generic-Vote: Set Random View Number",
                        List.of(GenericVote.class)) {
                    @Override
                    public void accept(FaultContext faultContext) {
                        Optional<Event> event = faultContext.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof GenericVote message)) {
                            throw invalidMessageTypeException;
                        }
                        EDHSNode originalNode = message.getNode();
                        EDHSNode newNode = null;
                        try {
                            newNode = new EDHSNode(originalNode.getParentHash(), originalNode.getClientRequest(), originalNode.getJustify(), (long) (Math.random() * 200));
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                        GenericVote mutatedMessage = new GenericVote(newNode.getHeight(), newNode, message.getPartialSignature().getSenderId());
                        mutatedMessage.sign(message.getSignedBy());

                        if(faultContext.getScenario() instanceof EDHotStuffScenario scenario)
                            scenario.log("MUTATION: Changed GENERIC-VOTE message (" + messageEvent.getSenderId() + " -> " + messageEvent.getRecipientId() + ") view from " + message.getViewNumber() + " to " + mutatedMessage.getViewNumber());
                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault(
                        "edhotstuff-generic-vote-rnd-parent",
                        "Generic-Vote: Set Random Parent",
                        List.of(GenericVote.class)) {
                    @Override
                    public void accept(FaultContext faultContext) {
                        Optional<Event> event = faultContext.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof GenericVote message)) {
                            throw invalidMessageTypeException;
                        }
                        EDHSNode originalNode = message.getNode();
                        EDHSNode newNode = null;
                        EDHotStuffReplica senderReplica = (EDHotStuffReplica) faultContext.getScenario().getNodes().get(messageEvent.getSenderId());
                        List<EDHSNode> availableNodes = senderReplica.getHashNodeMap().values().stream().filter(n -> !n.equals(originalNode) && ! n.getClientRequest().getRequestId().equals("GENESIS")).toList();
                        if(!availableNodes.isEmpty()) {
                            try {
                                String randomNodeHash = availableNodes.get((int) (Math.random() * availableNodes.size())).getHash();
                                newNode = new EDHSNode(randomNodeHash, originalNode.getClientRequest(), originalNode.getJustify(), originalNode.getHeight());

                                if(faultContext.getScenario() instanceof EDHotStuffScenario scenario)
                                    scenario.log("MUTATION: Changed GENERIC-VOTE message parent (" + messageEvent.getSenderId() + " -> " + messageEvent.getRecipientId() + ") from " + originalNode.getParentHash() + " to " + newNode.getParentHash());
                            } catch (NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            }
                        } else newNode = originalNode;
                        GenericVote mutatedMessage = new GenericVote(newNode.getHeight(), newNode, message.getPartialSignature().getSenderId());
                        mutatedMessage.sign(message.getSignedBy());

                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault(
                        "edhotstuff-generic-vote-rnd-qc",
                        "Generic-Vote: Set Random QC",
                        List.of(GenericVote.class)) {
                    @Override
                    public void accept(FaultContext faultContext) {
                        Optional<Event> event = faultContext.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof GenericVote message)) {
                            throw invalidMessageTypeException;
                        }
                        EDHSNode originalNode = message.getNode();
                        EDHSNode newNode = null;
                        EDHotStuffReplica senderReplica = (EDHotStuffReplica) faultContext.getScenario().getNodes().get(messageEvent.getSenderId());
                        List<EDHSNode> availableNodes = senderReplica.getHashNodeMap().values().stream().filter(n -> !n.equals(originalNode) && ! n.getClientRequest().getRequestId().equals("GENESIS")).toList();
                        if(!availableNodes.isEmpty()) {
                            try {
                                EDHSQuorumCertificate rndJustify = availableNodes.get((int) (Math.random() * availableNodes.size())).getJustify();
                                newNode = new EDHSNode(originalNode.getParentHash(), originalNode.getClientRequest(), rndJustify, originalNode.getHeight());

                                if(faultContext.getScenario() instanceof EDHotStuffScenario scenario)
                                    scenario.log("MUTATION: Changed GENERIC-VOTE message QC (" + messageEvent.getSenderId() + " -> " + messageEvent.getRecipientId() + ") from " + originalNode.getJustify().getNodeHash() + " to " + newNode.getJustify().getNodeHash());
                            } catch (NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            }
                        } else newNode = originalNode;
                        GenericVote mutatedMessage = new GenericVote(newNode.getHeight(), newNode, message.getPartialSignature().getSenderId());
                        mutatedMessage.sign(message.getSignedBy());

                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault(
                        "edhotstuff-generic-vote-rnd-qc-and-parent",
                        "Generic-Vote: Set Random QC and parent",
                        List.of(GenericVote.class)) {
                    @Override
                    public void accept(FaultContext faultContext) {
                        Optional<Event> event = faultContext.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof GenericVote message)) {
                            throw invalidMessageTypeException;
                        }
                        EDHSNode originalNode = message.getNode();
                        EDHSNode newNode = null;
                        EDHotStuffReplica senderReplica = (EDHotStuffReplica) faultContext.getScenario().getNodes().get(messageEvent.getSenderId());
                        List<EDHSNode> availableNodes = senderReplica.getHashNodeMap().values().stream().filter(n -> !n.equals(originalNode) && ! n.getClientRequest().getRequestId().equals("GENESIS")).toList();
                        if(!availableNodes.isEmpty()) {
                            try {
                                EDHSQuorumCertificate rndJustify = availableNodes.get((int) (Math.random() * availableNodes.size())).getJustify();
                                newNode = new EDHSNode(rndJustify.getNodeHash(), originalNode.getClientRequest(), rndJustify, originalNode.getHeight());

                                if(faultContext.getScenario() instanceof EDHotStuffScenario scenario)
                                    scenario.log("MUTATION: Changed GENERIC-VOTE message QC and parent (" + messageEvent.getSenderId() + " -> " + messageEvent.getRecipientId() + ") from parent: " + originalNode.getParentHash() + ", QC: " + originalNode.getJustify().getNodeHash() + " to parent: " + newNode.getParentHash() + ", QC: " + newNode.getJustify().getNodeHash());
                            } catch (NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            }
                        } else newNode = originalNode;
                        GenericVote mutatedMessage = new GenericVote(newNode.getHeight(), newNode, message.getPartialSignature().getSenderId());
                        mutatedMessage.sign(message.getSignedBy());

                        messageEvent.setPayload(mutatedMessage);
                    }
                },
                new MessageMutationFault(
                        "edhotstuff-generic-vote-rnd-client-request",
                        "Generic-Vote: Random Client Request",
                        List.of(GenericVote.class)) {
                    @Override
                    public void accept(FaultContext faultContext) {
                        Optional<Event> event = faultContext.getEvent();
                        if (event.isEmpty()) {
                            throw invalidMessageTypeException;
                        }
                        if (!(event.get() instanceof MessageEvent messageEvent)) {
                            throw invalidMessageTypeException;
                        }
                        if (!(messageEvent.getPayload() instanceof GenericVote message)) {
                            throw invalidMessageTypeException;
                        }
                        if(!(faultContext.getScenario() instanceof EDHotStuffScenario edHotStuffScenario)) {
                            throw invalidMessageTypeException;
                        }
                        EDHSNode originalNode = message.getNode();
                        EDHSNode newNode;
                        try {
                            HashSet<ClientRequest> pendingRequests = edHotStuffScenario.getPendingRequests();
                            pendingRequests.remove(originalNode.getClientRequest());
                            ClientRequest randomReq = pendingRequests.stream().toList().get((int) (Math.random() * pendingRequests.size()));
                            newNode = new EDHSNode(originalNode.getParentHash(), randomReq, originalNode.getJustify(), originalNode.getHeight());
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                        GenericVote mutatedMessage = new GenericVote(message.getViewNumber(), newNode, message.getPartialSignature().getSenderId());
                        mutatedMessage.sign(message.getSignedBy());

                        if(faultContext.getScenario() instanceof EDHotStuffScenario scenario)
                            scenario.log("MUTATION: Changed request of GENERIC-VOTE message (" + messageEvent.getSenderId() + " -> " + messageEvent.getRecipientId() + ") from " + originalNode.getClientRequest() + " to " + newNode.getClientRequest());
                        messageEvent.setPayload(mutatedMessage);
                    }
                }
        );
    }
}
