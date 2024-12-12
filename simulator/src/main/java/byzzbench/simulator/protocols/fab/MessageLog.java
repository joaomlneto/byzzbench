package byzzbench.simulator.protocols.fab;

import byzzbench.simulator.protocols.fab.messages.*;
import byzzbench.simulator.protocols.fab.replica.FabReplica;
import byzzbench.simulator.protocols.fab.replica.Pair;
import byzzbench.simulator.transport.DefaultClientRequestPayload;
import byzzbench.simulator.transport.Event;
import byzzbench.simulator.transport.MessagePayload;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Log
public class MessageLog {
    private final FabReplica replica;
    private SortedMap<String, Pair> acceptorsWithAcceptedProposal;
    @Getter
    private SortedMap<String, Pair> proposersWithLearnedValue;
    private SortedMap<String, Pair> satisfiedProposerNodes;
    private SortedMap<String, Pair> learnersWithLearnedValue;
    private List<String> nodesSuspectingLeader;
    @Getter
    private Pair learnedValue;
    private Pair acceptedProposal;
    @Getter
    private List<SignedResponse> responses;

    private final SortedMap<Long, List<ProposeMessage>> proposeMessages = new TreeMap<>();
    private final SortedMap<Long, List<AcceptMessage>> acceptMessages = new TreeMap<>();
    private final SortedMap<Long, List<LearnMessage>> learnMessages = new TreeMap<>();
    private final SortedMap<Long, List<SatisfiedMessage>> satisfiedMessages = new TreeMap<>();
    private final SortedMap<Long, List<ReplyMessage>> replyMessages = new TreeMap<>();
    private final SortedMap<Long, List<PullMessage>> pullMessages = new TreeMap<>();
    private final SortedMap<Long, List<SuspectMessage>> suspectMessages = new TreeMap<>();
    private final SortedMap<Long, List<QueryMessage>> queryMessages = new TreeMap<>();
    private final SortedMap<Long, List<ViewChangeMessage>> viewChangeMessages = new TreeMap<>();
    private final SortedMap<Long, List<NewViewMessage>> newViewMessages = new TreeMap<>();
    private final SortedMap<String, List<DefaultClientRequestPayload>> clientRequestMessages = new TreeMap<>();
    private final SortedMap<String, Integer> nodesAndProposalNumber = new TreeMap<>();

    public MessageLog(FabReplica replica) {
        this.replica = replica;
        this.acceptorsWithAcceptedProposal = new TreeMap<>();
        this.proposersWithLearnedValue = new TreeMap<>();
        this.satisfiedProposerNodes = new TreeMap<>();
        this.learnersWithLearnedValue = new TreeMap<>();
        this.nodesSuspectingLeader = new ArrayList<>();
        this.responses = new ArrayList<>();
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
        } else if (message instanceof NewViewMessage newViewMessage) {
            newViewMessages.computeIfAbsent(newViewMessage.getViewNumber(), k -> new ArrayList<>()).add(newViewMessage);
        } else if (message instanceof DefaultClientRequestPayload clientRequestMessage) {
            clientRequestMessages.computeIfAbsent(sender, k -> new ArrayList<>()).add(clientRequestMessage);
        }
    }

    public void resolveClientRequest(String clientId, Serializable request) {
        DefaultClientRequestPayload clientRequest = new DefaultClientRequestPayload(request);
        clientRequestMessages.get(clientId).remove(clientRequest);
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
        proposeMessages.get(proposeMessage.getValueAndProposalNumber().getNumber()).remove(proposeMessage);

        if (nodesAndProposalNumber.containsKey(senderId)) {
            if (proposeMessage.getValueAndProposalNumber().getNumber() > this.nodesAndProposalNumber.get(senderId)) {
                nodesAndProposalNumber.put(senderId, (int) proposeMessage.getValueAndProposalNumber().getNumber());
            }
        } else {
            nodesAndProposalNumber.put(senderId, (int) proposeMessage.getValueAndProposalNumber().getNumber());
        }

        Pair proposed = proposeMessage.getValueAndProposalNumber();
        long proposalNumber = proposeMessage.getValueAndProposalNumber().getNumber();
        byte[] messageProposedValue = proposeMessage.getValueAndProposalNumber().getValue();
        ProgressCertificate progressCertificate = proposeMessage.getProgressCertificate();

        if (proposalNumber != nodesAndProposalNumber.get(senderId)) {
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
        acceptMessages.get(viewNumber).remove(acceptMessage);
    }

    public boolean isAccepted(int quorum) {
        return acceptorsWithAcceptedProposal.size() >= quorum;
    }

    public void learnerOnLearned(String senderId, LearnMessage learnMessage) {
        Pair learnValue = learnMessage.getValueAndProposalNumber();
        learnersWithLearnedValue.put(senderId, learnValue);

        // Delete the message from the learnMessages map
        learnMessages.get(learnValue.getNumber()).remove(learnMessage);
    }

    public void learnerHasLearnedValue(Pair learnValue, int quorum) {
        AtomicInteger currentLearnedWithSamePair = new AtomicInteger();
        learnersWithLearnedValue.values().forEach(pair -> {
            if (pair.getNumber() == learnValue.getNumber() && Arrays.equals(pair.getValue(), learnValue.getValue())) {
                currentLearnedWithSamePair.getAndIncrement();
            }
        });

        if (currentLearnedWithSamePair.get() >= quorum && learnedValue == null) {
         this.learnedValue = learnValue;
        }
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
        satisfiedMessages.get(satisfiedValue.getNumber()).remove(satisfiedMessage);
    }

    public Pair onPull(String senderId, PullMessage pullMessage) {
        long pullValue = pullMessage.getViewNumber();
        pullMessages.get(pullValue).remove(pullMessage);
        return learnedValue != null ? learnedValue : null;
    }

    public boolean onSuspect(String sender, SuspectMessage suspectMessage, int quorum) {
        nodesSuspectingLeader.add(sender);
        suspectMessages.get(suspectMessage.getViewNumber()).remove(suspectMessage);

        return nodesSuspectingLeader.size() >= quorum;
    }

    public Pair onQuery(String sender, QueryMessage queryMessage, int quorum) {
        queryMessages.get(queryMessage.getViewNumber()).remove(queryMessage);

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
            return;
        }

        byte[] value = replyMessage.getValueAndProposalNumber().getValue();
        long viewNumber = replyMessage.getValueAndProposalNumber().getNumber();
        boolean isSigned = replyMessage.isSigned();
        String replySender = replyMessage.getSender();
        responses.add(new SignedResponse(value, viewNumber, isSigned, replySender));
    }

    public void acceptViewChange(ViewChangeMessage viewChangeMessage) {
        // Accept latest view change from viewChanges map
        if (viewChangeMessage.getProposalNumber() <= this.replica.getViewNumber()) {
            log.info("The view number of the VIEW-CHANGE message is not greater than the current view number");
            return;
        }

        this.replica.setView(viewChangeMessage.getProposalNumber());
        this.replica.setLeaderId(viewChangeMessage.getNewLeaderId());

        this.viewChangeMessages.remove(viewChangeMessage.getProposalNumber());
        for (Event event: this.replica.getTransport().getEventsInState(Event.Status.QUEUED)) {
            this.replica.getTransport().dropEvent(event.getEventId());
        }
        reset();
    }

    public void acceptNewView(NewViewMessage newViewMessage) {
        // Accept latest new view from newViews map
        if (newViewMessage.getViewNumber() <= this.replica.getViewNumber()) {
            log.info("The view number of the NEW-VIEW message is not greater than the current view number");
            return;
        }

        this.replica.setView(newViewMessage.getViewNumber());

        this.newViewMessages.remove(newViewMessage.getViewNumber());
        List<Event> queuedEvents = this.replica.getTransport().getEventsInState(Event.Status.QUEUED);
        log.info("Last event: " +  queuedEvents.getLast());

//        for (Event event: queuedEvents) {
//            this.replica.getTransport().dropEvent(event.getEventId());
//        }

        reset();
    }

    public ProgressCertificate electNewLeader(int quorum) {
        if (responses.size() < quorum) {
            log.warning("Leader " + replica.getId() + " did not receive enough responses to the QUERY message");
            return null;
        } else {
            log.info("Leader " + replica.getId() + " received enough responses to the QUERY message");
            // Combine the received responses into a progress certificate (PC)
            ProgressCertificate progressCertificate = new ProgressCertificate(this.replica.getViewNumber(), responses);
            if (pcIsValid(progressCertificate, quorum)) {
                return progressCertificate;
            } else {
                log.warning("Leader " + replica.getId() + " could not form a valid progress certificate");
                return null;
            }
        }
    }

    public boolean pcIsValid(ProgressCertificate progressCertificate, int quorum) {
        // If PC vouches for the value, update the value
        Optional<byte[]> vouchedValue = progressCertificate.majorityValue(quorum);
        if (vouchedValue.isPresent()) {
            return true;
        } else {
            log.warning("Progress certificate does not vouch for the value");
            return false;
        }
    }

    public boolean isSatisfied(int quorum) {
        return proposersWithLearnedValue.size() >= quorum;
    }

    public int satisfiedProposersCount() {
        return satisfiedProposerNodes.size();
    }

    public void deletePreviousRoundMessages(long proposalNumber) {
        acceptMessages.remove(proposalNumber);
        learnMessages.remove(proposalNumber);
        satisfiedMessages.remove(proposalNumber);
        replyMessages.remove(proposalNumber);
    }

    public int proposersWithLearnedValueCount() {
        return proposersWithLearnedValue.size();
    }
}
