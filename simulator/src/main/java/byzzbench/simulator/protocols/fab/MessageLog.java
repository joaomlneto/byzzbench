package byzzbench.simulator.protocols.fab;

import byzzbench.simulator.protocols.fab.messages.*;
import byzzbench.simulator.transport.DefaultClientRequestPayload;
import byzzbench.simulator.transport.MessagePayload;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Log
@Data
public class MessageLog {
    @JsonIgnore
    private final FastByzantineReplica replica;
    private SortedMap<String, Pair> acceptorsWithAcceptedProposal = new TreeMap<>();
    @Getter
    private SortedMap<String, Pair> proposersWithLearnedValue = new TreeMap<>();
    private SortedMap<String, Pair> satisfiedProposerNodes = new TreeMap<>();
    private SortedMap<String, Pair> learnersWithLearnedValue = new TreeMap<>();
    private SortedSet<String> nodesSuspectingLeader = new TreeSet<>();
    @Getter
    private Pair learnedValue;
    private Pair acceptedProposal;
    @Getter
    private SortedMap<String, SignedResponse> responses = new TreeMap<>();
    private SortedMap<Long, List<ProposeMessage>> proposeMessages = new TreeMap<>();
    private SortedMap<Long, List<AcceptMessage>> acceptMessages = new TreeMap<>();
    private SortedMap<Long, List<LearnMessage>> learnMessages = new TreeMap<>();
    private SortedMap<Long, List<SatisfiedMessage>> satisfiedMessages = new TreeMap<>();
    private SortedMap<Long, List<ReplyMessage>> replyMessages = new TreeMap<>();
    private SortedMap<Long, List<PullMessage>> pullMessages = new TreeMap<>();
    private SortedMap<Long, List<SuspectMessage>> suspectMessages = new TreeMap<>();
    private SortedMap<Long, List<QueryMessage>> queryMessages = new TreeMap<>();
    private SortedMap<Long, List<ViewChangeMessage>> viewChangeMessages = new TreeMap<>();
    private SortedMap<String, List<DefaultClientRequestPayload>> clientRequestMessages = new TreeMap<>();

    public MessageLog(FastByzantineReplica replica) {
        this.replica = replica;
        this.acceptorsWithAcceptedProposal = new TreeMap<>();
        this.proposersWithLearnedValue = new TreeMap<>();
        this.satisfiedProposerNodes = new TreeMap<>();
        this.learnersWithLearnedValue = new TreeMap<>();
        this.nodesSuspectingLeader = new TreeSet<>();
        this.responses = new TreeMap<>();
        proposeMessages = new TreeMap<>();
        acceptMessages = new TreeMap<>();
        learnMessages = new TreeMap<>();
        satisfiedMessages = new TreeMap<>();
        replyMessages = new TreeMap<>();
        pullMessages = new TreeMap<>();
        suspectMessages = new TreeMap<>();
        queryMessages = new TreeMap<>();
        viewChangeMessages = new TreeMap<>();
        clientRequestMessages = new TreeMap<>();
    }

    public void addMessage(String sender, MessagePayload message) {
        if (message instanceof ProposeMessage) {
            proposeMessages.computeIfAbsent(((ProposeMessage) message).getValueAndProposalNumber().getNumber(), k -> new ArrayList<>()).add((ProposeMessage) message);
        } else if (message instanceof AcceptMessage acceptMessage) {
            acceptMessages.computeIfAbsent(acceptMessage.getValueAndProposalNumber().getNumber(), k -> new ArrayList<>()).add(acceptMessage);
        } else if (message instanceof LearnMessage learnMessage) {
            learnMessages.computeIfAbsent(learnMessage.getValueAndProposalNumber().getNumber(), k -> new ArrayList<>()).add(learnMessage);
        } else if (message instanceof SatisfiedMessage satisfiedMessage) {
            satisfiedMessages.computeIfAbsent(satisfiedMessage.getValueAndProposalNumber().getNumber(), k -> new ArrayList<>()).add(satisfiedMessage);
        } else if (message instanceof ReplyMessage replyMessage) {
            replyMessages.computeIfAbsent(replyMessage.getValueAndProposalNumber().getNumber(), k -> new ArrayList<>()).add(replyMessage);
        } else if (message instanceof PullMessage pullMessage) {
            pullMessages.computeIfAbsent(pullMessage.getViewNumber(), k -> new ArrayList<>()).add(pullMessage);
        } else if (message instanceof SuspectMessage suspectMessage) {
            suspectMessages.computeIfAbsent(suspectMessage.getViewNumber(), k -> new ArrayList<>()).add(suspectMessage);
        } else if (message instanceof QueryMessage queryMessage) {
            queryMessages.computeIfAbsent(queryMessage.getViewNumber(), k -> new ArrayList<>()).add(queryMessage);
        } else if (message instanceof ViewChangeMessage viewChangeMessage) {
            viewChangeMessages.computeIfAbsent(viewChangeMessage.getProposalNumber(), k -> new ArrayList<>()).add(viewChangeMessage);
        } else if (message instanceof DefaultClientRequestPayload clientRequestMessage) {
            clientRequestMessages.computeIfAbsent(sender, k -> new ArrayList<>()).add(clientRequestMessage);
        }
    }

    public void resolveClientRequest(String clientId, Serializable request) {
        DefaultClientRequestPayload clientRequest = new DefaultClientRequestPayload(request);
        if (clientRequestMessages.containsKey(clientId)) clientRequestMessages.get(clientId).remove(clientRequest);
        reset();
        learnedValue = null;
        acceptedProposal = null;
    }

    public void resolve() {
        reset();
        learnedValue = null;
        acceptedProposal = null;
    }

    public void reset() {
        acceptorsWithAcceptedProposal.clear();
        proposersWithLearnedValue.clear();
        satisfiedProposerNodes.clear();
        learnersWithLearnedValue.clear();
        nodesSuspectingLeader.clear();
        responses.clear();
        proposeMessages.clear();
        acceptMessages.clear();
        learnMessages.clear();
        satisfiedMessages.clear();
        replyMessages.clear();
        pullMessages.clear();
        suspectMessages.clear();
        queryMessages.clear();
        viewChangeMessages.clear();
        clientRequestMessages.clear();
    }

    public boolean onPropose(String senderId, ProposeMessage proposeMessage, int vouchingThreshold) {
        // If the PROPOSE message has a higher round number than the current round number, update the round number
        // Remove message from the proposeMessages queue
        long proposalNumber = proposeMessage.getValueAndProposalNumber().getNumber();
        if (proposeMessages.containsKey(proposalNumber)) {
            proposeMessages.get(proposalNumber).remove(proposeMessage);
        }

        Pair proposed = proposeMessage.getValueAndProposalNumber();
        byte[] messageProposedValue = proposeMessage.getValueAndProposalNumber().getValue();
        ProgressCertificate progressCertificate = proposeMessage.getProgressCertificate();

        if (proposalNumber != this.replica.getViewNumber()) {
            log.info("The view number of the PROPOSE message is not the same as the current view number");
            return false; // Only listen to current leader
        }

        if (acceptedProposal != null && acceptedProposal.getNumber() == proposalNumber) {
            log.info("duplicate proposal");
            return false; // Ignore duplicate proposals
        }

        if (acceptedProposal != null && !Arrays.equals(acceptedProposal.getValue(), messageProposedValue) &&
                !progressCertificate.vouchesFor(messageProposedValue, vouchingThreshold)) {
            log.info("The proposal is not vouched for by the progress certificate");
            return false; // Ignore proposals that are not vouched for by the progress certificate
        }

        acceptedProposal = proposed; // Accept the proposal
        return true;
    }

    public void onAccept(String senderId, AcceptMessage acceptMessage) {
        Pair acceptValue = acceptMessage.getValueAndProposalNumber();
        acceptorsWithAcceptedProposal.put(senderId, acceptValue);

        byte[] acceptedValue = acceptMessage.getValueAndProposalNumber().getValue();
        long viewNumber = acceptMessage.getValueAndProposalNumber().getNumber();

        AtomicInteger currentAccepted = new AtomicInteger();
        // If there are acceptedThreshold accepted values for the same proposalValue, send a LEARN message to all Proposer replicas
        acceptorsWithAcceptedProposal.values().forEach(pair -> {
            if (pair.getNumber() == this.replica.getViewNumber() && Arrays.equals(pair.getValue(), acceptedValue)) {
                currentAccepted.getAndIncrement();
            }
        });

        log.info("The number of accepted values for the same proposal value is " + currentAccepted.get());
        this.learnedValue = new Pair(this.replica.getViewNumber(), acceptedValue);
        // Remove message from the acceptMessages map
        if (acceptMessages.containsKey(viewNumber)) acceptMessages.get(viewNumber).remove(acceptMessage);
    }

    public boolean isAccepted(int quorum) {
        return acceptorsWithAcceptedProposal.size() >= quorum;
    }

    public void learnerOnLearned(String senderId, LearnMessage learnMessage) {
        Pair learnValue = learnMessage.getValueAndProposalNumber();
        learnersWithLearnedValue.put(senderId, learnValue);

        // Delete the message from the learnMessages map
        if (learnMessages.containsKey(learnValue.getNumber())) learnMessages.get(learnValue.getNumber()).remove(learnMessage);
    }

    public boolean learnerHasLearnedValue(Pair learnValue, int quorum) {
        AtomicInteger currentLearnedWithSamePair = new AtomicInteger();
        learnersWithLearnedValue.values().forEach(pair -> {
            if (Objects.equals(pair.getNumber(), learnValue.getNumber()) && Arrays.equals(pair.getValue(), learnValue.getValue())) {
                currentLearnedWithSamePair.getAndIncrement();
            }
        });

        if (currentLearnedWithSamePair.get() >= quorum && learnedValue == null) {
            this.learnedValue = learnValue;
            return true;
        }

        return false;
    }

    public boolean proposerHasLearned(int quorum) {
        if (proposersWithLearnedValue.size() >= quorum) {
            return true;
        }

        nodesSuspectingLeader.add(replica.getId());
        return false;
    }

    public boolean isLearned(String senderId, LearnMessage learnMessage, int quorum) {
        Pair learnValue = learnMessage.getValueAndProposalNumber();
        proposersWithLearnedValue.put(senderId, learnValue);

        return proposersWithLearnedValue.size() >= quorum;
    }

    public void onSatisfied(String senderId, SatisfiedMessage satisfiedMessage) {
        Pair satisfiedValue = satisfiedMessage.getValueAndProposalNumber();
        satisfiedProposerNodes.computeIfAbsent(senderId, k -> satisfiedValue);
        // Remove the message from the satisfiedMessages map
        if (satisfiedMessages.containsKey(satisfiedValue.getNumber())) satisfiedMessages.get(satisfiedValue.getNumber()).remove(satisfiedMessage);
    }

    public Pair onPull(String senderId, PullMessage pullMessage) {
        long viewNumber = pullMessage.getViewNumber();
        if (pullMessages.containsKey(viewNumber)) pullMessages.get(viewNumber).remove(pullMessage);
        return learnedValue != null ? learnedValue : null;
    }

    public boolean onSuspect(String sender, SuspectMessage suspectMessage, int quorum) {
        nodesSuspectingLeader.add(sender);
        if (suspectMessages.containsKey(suspectMessage.getViewNumber())) {
            suspectMessages.get(suspectMessage.getViewNumber()).remove(suspectMessage);
        }

        return nodesSuspectingLeader.size() >= quorum;
    }

    public Pair onQuery(String sender, QueryMessage queryMessage, int quorum) {
        if (queryMessages.containsKey(queryMessage.getViewNumber())) {
            queryMessages.get(queryMessage.getViewNumber()).remove(queryMessage);
        }

        long messageViewNumber = queryMessage.getViewNumber();

        // Ignore bad requests.
        if (messageViewNumber < this.replica.getViewNumber()) {
            return null;
        }

        // Update the view number.
        this.replica.setView(messageViewNumber);

        if (learnedValue == null) return new Pair(messageViewNumber, new byte[0]);

        return learnedValue;
    }

    public void onReply(String sender, ReplyMessage replyMessage) {
        replyMessages.get(replyMessage.getValueAndProposalNumber().getNumber()).remove(replyMessage);

        if (replyMessage.getValueAndProposalNumber().getNumber() != this.replica.getViewNumber()) {
            log.info("The view number of the REPLY message is not the same as the current view number");
            return;
        }

        byte[] value = replyMessage.getValueAndProposalNumber().getValue();
        long viewNumber = replyMessage.getValueAndProposalNumber().getNumber();
        boolean isSigned = replyMessage.isSigned();
        String replySender = replyMessage.getSender();
        responses.put(sender, new SignedResponse(value, viewNumber, isSigned, replySender));
        // Log all responses
        log.info("REPLY is : " + replyMessage.getValueAndProposalNumber().getNumber() + " " + Arrays.toString(replyMessage.getValueAndProposalNumber().getValue()));
        for (Map.Entry<String, SignedResponse> response : responses.entrySet()) {
            log.info("Responses map has the following elements: " + response.getKey() + " " + Arrays.toString(response.getValue().getValue()));
        }
    }

    public void acceptViewChange(ViewChangeMessage viewChangeMessage) {
        // Accept latest view change from viewChanges map
        if (viewChangeMessage.getProposalNumber() < this.replica.getViewNumber()) {
            log.info(this.replica.getViewNumber() + " " + viewChangeMessage.getProposalNumber());
            log.info("The view number of the VIEW-CHANGE message is not greater than the current view number");
            return;
        }

        reset();
    }

    public boolean isSatisfied(int quorum) {
        if (proposersWithLearnedValue != null) return proposersWithLearnedValue.size() >= quorum;
        return false;
    }

    public int satisfiedProposersCount() {
        return satisfiedProposerNodes.size();
    }
}
