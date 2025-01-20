package byzzbench.simulator.protocols.fab2;

import byzzbench.simulator.protocols.fab2.messages.*;
import byzzbench.simulator.transport.DefaultClientRequestPayload;
import byzzbench.simulator.transport.MessagePayload;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Log
@Data
public class MessageLog {
    @JsonIgnore
    private FastByzantineReplica replica = null;
//    private NewFastByzantineReplica newFastByzantineReplica = null;
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
    private SortedMap<ProposalNumber, List<ProposeMessage>> proposeMessages = new TreeMap<>();
    private SortedMap<ProposalNumber, List<AcceptMessage>> acceptMessages = new TreeMap<>();
    private SortedMap<ProposalNumber, List<LearnMessage>> learnMessages = new TreeMap<>();
    private SortedMap<ProposalNumber, List<SatisfiedMessage>> satisfiedMessages = new TreeMap<>();
    private SortedMap<ProposalNumber, List<ReplyMessage>> replyMessages = new TreeMap<>();
    private SortedMap<ProposalNumber, List<PullMessage>> pullMessages = new TreeMap<>();
    // Takes into consideration only the view number
    private SortedMap<Long, List<SuspectMessage>> suspectMessages = new TreeMap<>();
    // Takes into consideration only the view number
    private SortedMap<Long, List<QueryMessage>> queryMessages = new TreeMap<>();
    private SortedMap<ProposalNumber, List<ViewChangeMessage>> viewChangeMessages = new TreeMap<>();
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
            proposeMessages.computeIfAbsent(((ProposeMessage) message).getValueAndProposalNumber().getProposalNumber(), k -> new ArrayList<>()).add((ProposeMessage) message);
        } else if (message instanceof AcceptMessage acceptMessage) {
            acceptMessages.computeIfAbsent(acceptMessage.getValueAndProposalNumber().getProposalNumber(), k -> new ArrayList<>()).add(acceptMessage);
        } else if (message instanceof LearnMessage learnMessage) {
            learnMessages.computeIfAbsent(learnMessage.getValueAndProposalNumber().getProposalNumber(), k -> new ArrayList<>()).add(learnMessage);
        } else if (message instanceof SatisfiedMessage satisfiedMessage) {
            satisfiedMessages.computeIfAbsent(satisfiedMessage.getValueAndProposalNumber().getProposalNumber(), k -> new ArrayList<>()).add(satisfiedMessage);
        } else if (message instanceof ReplyMessage replyMessage) {
            replyMessages.computeIfAbsent(replyMessage.getValueAndProposalNumber().getProposalNumber(), k -> new ArrayList<>()).add(replyMessage);
        } else if (message instanceof PullMessage pullMessage) {
            pullMessages.computeIfAbsent(pullMessage.getProposalNumber(), k -> new ArrayList<>()).add(pullMessage);
        } else if (message instanceof SuspectMessage suspectMessage) {
            suspectMessages.computeIfAbsent(suspectMessage.getProposalNumber().getViewNumber(), k -> new ArrayList<>()).add(suspectMessage);
        } else if (message instanceof QueryMessage queryMessage) {
            queryMessages.computeIfAbsent(queryMessage.getProposalNumber().getViewNumber(), k -> new ArrayList<>()).add(queryMessage);
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
        // Remove message from the proposeMessages queue
        ProposalNumber proposalNumber = proposeMessage.getValueAndProposalNumber().getProposalNumber();
        if (proposeMessages.containsKey(proposalNumber)) {
            proposeMessages.get(proposalNumber).remove(proposeMessage);
        }

        Pair proposed = proposeMessage.getValueAndProposalNumber();
        byte[] messageProposedValue = proposeMessage.getValueAndProposalNumber().getValue();
        ProgressCertificate progressCertificate = proposeMessage.getProgressCertificate();

        // Checking if the proposed value is for the same view and sequence number
        if (proposalNumber.getViewNumber() != this.replica.getViewNumber()) {
            log.info("The view number of the PROPOSE message is not the same as the current view number");
            return false; // Only listen to current leader
        } else if (proposalNumber.getSequenceNumber() != this.replica.getProposalNumber()) {
            log.info("The sequence number of the PROPOSE message is not the same as the current sequence number");
            return false; // Ignore proposals with different sequence numbers
        }

        // If the accepted proposal has the same view and sequence number as the proposed value, ignore the proposal
        if (acceptedProposal != null && acceptedProposal.getProposalNumber().equals(proposalNumber)) {
            log.info("Duplicate proposal received.");
            return false; // Ignore duplicate proposals
        }

        // Accepted value changes only with valid progress certificate
        if (acceptedProposal != null && !Arrays.equals(acceptedProposal.getValue(), messageProposedValue) && progressCertificate != null &&
                !progressCertificate.vouchesFor(messageProposedValue, vouchingThreshold)) {
            log.info("The proposal is not vouched for by the progress certificate");
            return false; // Ignore proposals that are not vouched for by the progress certificate
        }

        if (acceptedProposal != null && !Arrays.equals(acceptedProposal.getValue(), messageProposedValue) && progressCertificate == null) {
            log.info("The proposal is not vouched for by the progress certificate");
            return false; // Ignore proposals that are not vouched for by the progress certificate
        }

        // Accept the proposal
        acceptedProposal = proposed;
        return true;
    }

    public boolean onAccept(String senderId, AcceptMessage acceptMessage, int threshold) {
        Pair acceptValue = acceptMessage.getValueAndProposalNumber();
        acceptorsWithAcceptedProposal.put(senderId, acceptValue);

        byte[] acceptedValue = acceptMessage.getValueAndProposalNumber().getValue();
        ProposalNumber proposalNumber = acceptMessage.getValueAndProposalNumber().getProposalNumber();

        AtomicInteger currentAccepted = new AtomicInteger();
        // If there are acceptedThreshold accepted values for the same proposalValue, send a LEARN message to all Proposer replicas
        acceptorsWithAcceptedProposal.values().forEach(pair -> {
            if (pair.getProposalNumber().getViewNumber() == this.replica.getViewNumber()
                    && pair.getProposalNumber().getSequenceNumber() == proposalNumber.getSequenceNumber()
                    && Arrays.equals(pair.getValue(), acceptedValue)) {
                currentAccepted.getAndIncrement();
            }
        });

        log.info("The number of accepted values for the same proposal value is " + currentAccepted.get());
        if (currentAccepted.get() >= threshold) {
            log.info("The learner has learned the value");
            // TODO: Changed here proposal number
            this.learnedValue = new Pair(acceptedValue, proposalNumber);
            return true;
        }

        // Remove message from the acceptMessages map
//        if (acceptMessages.containsKey(proposalNumber)) acceptMessages.get(proposalNumber).remove(acceptMessage);

        return false;
    }

    public boolean isAccepted(int quorum) {
        return acceptorsWithAcceptedProposal.size() >= quorum;
    }

    public void learnerOnLearned(String senderId, LearnMessage learnMessage) {
        Pair learnValue = learnMessage.getValueAndProposalNumber();
        long viewNumber = learnValue.getProposalNumber().getViewNumber();
        long sequenceNumber = learnValue.getProposalNumber().getSequenceNumber();

        if (viewNumber != this.replica.getViewNumber()) {
            log.info("The view number of the LEARN message is not the same as the current view number");
            return;
        } else if (sequenceNumber != this.replica.getProposalNumber()) {
            log.info("The sequence number of the LEARN message is not the same as the current sequence number");
            return;
        }

        learnersWithLearnedValue.put(senderId, learnValue);

        // Delete the message from the learnMessages map
        if (learnMessages.containsKey(learnValue.getProposalNumber())) learnMessages.get(learnValue.getProposalNumber()).remove(learnMessage);
    }

    public boolean learnerHasLearnedValue(Pair learnValue, int quorum) {
        AtomicInteger currentLearnedWithSamePair = new AtomicInteger();
        learnersWithLearnedValue.values().forEach(pair -> {
            if (Objects.equals(pair.getProposalNumber(), learnValue.getProposalNumber()) && Arrays.equals(pair.getValue(), learnValue.getValue())) {
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
        long viewNumber = learnValue.getProposalNumber().getViewNumber();
        long sequenceNumber = learnValue.getProposalNumber().getSequenceNumber();

        if (learnValue.getProposalNumber().getViewNumber() != this.replica.getViewNumber()) {
            log.info("The view number of the LEARN message is not the same as the current view number");
            return false;
        } else if (learnValue.getProposalNumber().getSequenceNumber() != this.replica.getProposalNumber()) {
            log.info("The sequence number of the LEARN message is not the same as the current sequence number");
            return false;
        }

        proposersWithLearnedValue.put(senderId, learnValue);

        return proposersWithLearnedValue.size() >= quorum;
    }

    public void onSatisfied(String senderId, SatisfiedMessage satisfiedMessage) {
        Pair satisfiedValue = satisfiedMessage.getValueAndProposalNumber();
        satisfiedProposerNodes.computeIfAbsent(senderId, k -> satisfiedValue);
        // Remove the message from the satisfiedMessages map
        if (satisfiedMessages.containsKey(satisfiedValue.getProposalNumber())) satisfiedMessages.get(satisfiedValue.getProposalNumber()).remove(satisfiedMessage);
    }

    public Pair onPull(String senderId, PullMessage pullMessage) {
        ProposalNumber proposalNumber = pullMessage.getProposalNumber();
        if (pullMessages.containsKey(proposalNumber)) pullMessages.get(proposalNumber).remove(pullMessage);
        return learnedValue;
    }

    public boolean onSuspect(String sender, SuspectMessage suspectMessage, int quorum) {
        nodesSuspectingLeader.add(sender);
        if (suspectMessages.containsKey(suspectMessage.getProposalNumber().getViewNumber())) {
            suspectMessages.get(suspectMessage.getProposalNumber().getViewNumber()).remove(suspectMessage);
        }

        return nodesSuspectingLeader.size() >= quorum;
    }

    public Pair onQuery(String sender, QueryMessage queryMessage, int quorum) {
        if (queryMessages.containsKey(queryMessage.getViewNumber())) {
            queryMessages.get(queryMessage.getViewNumber()).remove(queryMessage);
        }

        long messageViewNumber = queryMessage.getViewNumber();

        // Ignore bad requests.
//        if (messageViewNumber < this.replica.getViewNumber()) {
//            return null;
//        }

        return acceptedProposal;
    }

    public void onReply(String sender, ReplyMessage replyMessage) {
        replyMessages.get(replyMessage.getValueAndProposalNumber().getProposalNumber()).remove(replyMessage);

        if (replyMessage.getValueAndProposalNumber() == null) {
            log.info("The REPLY message does not contain a value and proposal number");
            return;
        }

        byte[] value = replyMessage.getValueAndProposalNumber().getValue();
        long viewNumber = replyMessage.getValueAndProposalNumber().getProposalNumber().getViewNumber();
        long sequenceNumber = replyMessage.getValueAndProposalNumber().getProposalNumber().getSequenceNumber();
        boolean isSigned = replyMessage.isSigned();
        String replySender = replyMessage.getSender();
        responses.put(sender, new SignedResponse(value, replyMessage.getValueAndProposalNumber().getProposalNumber(), isSigned, replySender));
    }

    public void acceptViewChange(ViewChangeMessage viewChangeMessage) {
        // Accept latest view change from viewChanges map
        if (viewChangeMessage.getProposalNumber().getViewNumber() < this.replica.getViewNumber()) {
            log.info(this.replica.getViewNumber() + " " + viewChangeMessage.getProposalNumber());
            log.info("The view number of the VIEW-CHANGE message is not greater than the current view number");
            return;
        }

        reset();
    }

    public boolean isSatisfied(int quorum) {
        if (satisfiedProposerNodes != null) return satisfiedProposersCount() >= quorum;
        return false;
    }

    public int satisfiedProposersCount() {
        return satisfiedProposerNodes.size();
    }
}
