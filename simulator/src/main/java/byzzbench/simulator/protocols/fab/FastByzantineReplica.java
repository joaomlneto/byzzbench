package byzzbench.simulator.protocols.fab;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.protocols.fab.messages.*;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.DefaultClientRequestPayload;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A replica in the FAB protocol.
 */
@Log
@Getter
public class FastByzantineReplica extends LeaderBasedProtocolReplica {
    // In the "Fast Byzantine Consensus" protocol, a replica can have one or more roles.
    // The possible roles are : PROPOSER, ACCEPTOR, LEARNER.
    // The required number of replicas with each role is:
    // p ( proposers ) = 3 * f + 1, a ( acceptors ) = 5 * f + 1, l ( learners ) = 3 * f + 1.
    private final List<Role> roles;

    // The number of replicas in the system with each role.
    private final int p, a, l, f;
    // The message round number.
    private long viewNumber;
    // The value that the leader replica is proposing - changes each round, depending on the client request.
    private byte[] proposedValue;
    // The progress certificate for the current view.
    private ProgressCertificate pc;
    // Timeout until the proposer replica starts suspecting the leader for the lack of progress.
    private final long messageTimeoutDuration;
    // Current leader.
    @Setter
    private String leaderId;
    @Setter
    private AtomicBoolean isCurrentlyLeader = new AtomicBoolean(false);

    // The set of node IDs in the system for each role.
    private final SortedSet<String> acceptorNodeIds;
    private final SortedSet<String> learnerNodeIds;
    private final SortedSet<String> proposerNodeIds;
    private final SortedSet<String> nodeIds;

    private final Transport transport;

    // Current client ID that the system is communicating with.
    private String clientId;

    private boolean isSatisfied = false;
    // Keep track of the timeouts set by the replica.
    private long learnerTimeoutId = -1;
    private long proposerTimeoutId = -1;
    private long leaderTimeoutId = -1;
    // To be set during the recovery protocol.
    @Setter
    private boolean isRecovered = true;
    /**
     * The log of received messages for the replica.
     */
    @JsonIgnore
    private MessageLog messageLog;

    public FastByzantineReplica(
            String nodeId,
            SortedSet<String> nodeIds,
            Transport transport,
            Scenario scenario,
            List<Role> roles,
            boolean isCurrentlyLeader,
            long messageTimeoutDuration,
            int p, int a, int l, int f,
            SortedSet<String> acceptorNodeIds,
            SortedSet<String> learnerNodeIds,
            SortedSet<String> proposerNodeIds,
            String leaderId) {
        super(nodeId, scenario, new TotalOrderCommitLog());
        this.roles = roles;
        this.nodeIds = nodeIds;
        this.transport = transport;
        this.messageTimeoutDuration = messageTimeoutDuration;
        this.isCurrentlyLeader.set(isCurrentlyLeader);
        this.p = p;
        this.a = a;
        this.l = l;
        this.f = f;
        this.acceptorNodeIds = acceptorNodeIds;
        this.learnerNodeIds = learnerNodeIds;
        this.proposerNodeIds = proposerNodeIds;
        this.leaderId = leaderId;
        this.messageLog = new MessageLog(this);
    }

    @Override
    public void initialize() {
        // Starting the protocol - a new leader has been elected only once
        this.viewNumber = 1;
        messageLog = new MessageLog(this);
        onStart();
    }

    public void onStart() {
        log.info("Replica " + getId() + " is starting");
        if (isProposer()) proposerOnStart();
        if (isLearner()) learnerOnStart();
    }

    /**
     * LEADER LOGIC
     * The LEADER starts by sending a PROPOSE message to all ACCEPTOR nodes. It keeps sending the message until
     * the threshold - ceil((p + f + 1) / 2) - is reached.
     */
    private void leaderOnStart(ProposeMessage proposeMessage) {
        log.info("Replica " + getId() + " is the leader and preparing to send a PROPOSE message to all ACCEPTOR nodes");
        SortedSet<String> acceptorReceipts = new TreeSet<>(List.of("A", "B", "C", "D", "E", "F"));

        this.leaderTimeoutId = this.setTimeout("leader-timeout", () -> {
            log.info("Timer was set");
            this.multicastMessage(proposeMessage, acceptorReceipts);
        }, Duration.ofMillis(10000));
    }

    /**
     * PROPOSER LOGIC
     * The PROPOSER starts by waiting to learn the accepted value from the ACCEPTOR nodes.
     * If the PROPOSER does not receive enough LEARN messages, it suspects the leader due to the lack of progress.
     */
    private void proposerOnStart() {
        int learnedThreshold = (int) Math.ceil((l + f + 1) / 2.0);

         this.proposerTimeoutId = this.setTimeout("proposer-timeout", () -> {
             if (!messageLog.proposerHasLearned(learnedThreshold)) {
                 log.warning("Leader suspected by proposer " + getId());
                 broadcastMessageIncludingSelf(new SuspectMessage(getId(), this.leaderId, this.viewNumber));
             }
         }, Duration.ofMillis(messageTimeoutDuration));
    }

    /**
     * LEARNER LOGIC
     * The LEARNER, if it has not learned a value, sends a PULL message to all LEARNER nodes.
     */
    private void learnerOnStart() {
        if (messageLog.getLearnedValue() == null) {
            log.info("Learner " + getId() + " has not learned a value. Sending PULL message...");
            this.learnerTimeoutId = this.setTimeout("learner-timeout",
                    () -> multicastMessage(new PullMessage(this.viewNumber), this.learnerNodeIds),
                    Duration.ofMillis(90000));
        }
    }

    /**
     * Handle a client request received by the replica.
     * @param clientId the ID of the client
     * @param request  the request payload
     */
    @Override
    public void handleClientRequest(String clientId, Serializable request) throws UnsupportedOperationException {
        // If the client request is not send to the leader, forward it to the leader.
        if (!isLeader()) {
            this.sendMessage(new ForwardClientRequest(request, clientId), this.leaderId);
            return;
        }

        // Sending PROPOSE message to all ACCEPTOR nodes.
        this.clientId = clientId;
        if (pc == null) { proposedValue = request.toString().getBytes(); }
        ProposeMessage proposeMessage = new ProposeMessage(this.getId(), new Pair(this.viewNumber, this.proposedValue), this.pc);
        this.multicastMessage(proposeMessage, this.nodeIds);
        leaderOnStart(proposeMessage);
        onStart();
        // I don't think this should be here
        messageLog.resolveClientRequest(clientId, request);
    }

    @Override
    public void handleMessage(String sender, MessagePayload message) throws UnsupportedOperationException {
        messageLog.addMessage(sender, message);
        switch (message) {
            case ProposeMessage proposeMessage when isAcceptor() -> handleProposeMessage(sender, proposeMessage);
            case AcceptMessage acceptMessage when isLearner() -> handleAcceptMessage(sender, acceptMessage);
            case SatisfiedMessage satisfiedMessage when isProposer() ->
                    handleSatisfiedMessage(sender, satisfiedMessage);
            case LearnMessage learnMessage -> {
                if (isProposer()) handleLearnMessageProposer(sender, learnMessage);
                if (isLearner()) handleLearnMessageLearner(sender, learnMessage);
            }
            case PullMessage pullMessage when isLearner() -> handlePullMessage(sender, pullMessage);
            case QueryMessage queryMessage when isAcceptor() -> handleQueryMessage(sender, queryMessage);
            case SuspectMessage suspectMessage -> handleSuspectMessage(sender, suspectMessage);
            case ReplyMessage replyMessage -> handleReplyMessage(sender, replyMessage);
            case ViewChangeMessage viewChangeMessage -> handleViewChangeMessage(sender, viewChangeMessage);
            case DefaultClientRequestPayload clientRequest -> {
                this.clientId = sender;
                handleClientRequest(sender, clientRequest.getOperation());
            }
            case ForwardClientRequest forwardClientRequest -> {
                this.clientId = forwardClientRequest.getClientId();
                handleClientRequest(sender, forwardClientRequest.getOperation());
            }
            case null, default -> {
                String messageType = message == null ? "null" : message.getType();
                throw new UnsupportedOperationException("Unknown message type: " + messageType);
            }
        }
    }

    /**
     * Handle a PROPOSE message send by a replica with Proposer role, who is the leader, received by all Acceptor replicas.
     * @param sender : the nodeId of the sender (the current leader)
     * @param proposeMessage : the PROPOSE message with the proposed value and round number
     */
    private void handleProposeMessage(String sender, ProposeMessage proposeMessage) {
        long proposalNumber = proposeMessage.getValueAndProposalNumber().getNumber();
        // The protocol is in a new view, ignore past proposals
        if (this.viewNumber > proposalNumber) {
            log.info("Acceptor " + getId() + " received PROPOSE message with an outdated proposal number");
            return;
        }

        // The protocol has moved on to the next view, so we need to reset it.
        if (this.viewNumber < proposalNumber) {
            reset(proposalNumber, sender, proposeMessage);
            this.isSatisfied = false;
        }

        int vouchingThreshold = (int) Math.ceil((a - f + 1) / 2.0);

        if (messageLog.onPropose(sender, proposeMessage, vouchingThreshold)) {
            log.info("Acceptor " + getId() + " accepted proposal with value " +
                    new String(proposeMessage.getValueAndProposalNumber().getValue()));
            multicastMessage(new AcceptMessage(getId(), proposeMessage.getValueAndProposalNumber()), this.learnerNodeIds);
        }
    }

    /**
     * Handle an ACCEPT message sent by an Acceptor replica, received by a Learner replica.
     * @param sender : the nodeId of the sender (an Acceptor replica)
     * @param acceptMessage : the ACCEPT message with the value and proposal number
     */
    private void handleAcceptMessage(String sender, AcceptMessage acceptMessage) {
        long proposalNumber = acceptMessage.getValueAndProposalNumber().getNumber();
        if (this.viewNumber > proposalNumber) {
            log.info("Learner " + getId() + " received ACCEPT message with an outdated proposal number");
            return;
        }

        if (this.viewNumber < proposalNumber) {
            reset(proposalNumber, sender, acceptMessage);
        }

        log.info("Learner " + getId() + " received ACCEPT from " + sender + " and proposal number " + proposalNumber);
        int acceptedThreshold = (int) Math.ceil((a + (3 * f) + 1) / 2.0);
        messageLog.onAccept(sender, acceptMessage);

        if (messageLog.isAccepted(acceptedThreshold)) {
            log.info("Acceptor " + getId() + " sending LEARN to all proposer...");
            Pair acceptValue = acceptMessage.getValueAndProposalNumber();
            multicastMessage(new LearnMessage(acceptValue), this.proposerNodeIds);
        }
    }

    /**
     * Handle a LEARN message sent by a Proposer replica, received by a Proposer replica.
     * @param sender : the nodeId of the sender (a Learner replica)
     * @param learnMessage : the LEARN message with the value and proposal number
     */
    private void handleLearnMessageProposer(String sender, LearnMessage learnMessage) {
        // Should never be the case for this
        if (this.viewNumber > learnMessage.getValueAndProposalNumber().getNumber()) {
            log.info("Proposer " + getId() + " received LEARN message with an outdated proposal number");
            return;
        }

        log.info("Proposer " + getId() + " received LEARN from " + sender + " and proposal number " + learnMessage.getValueAndProposalNumber().getNumber());
        Pair learnValue = learnMessage.getValueAndProposalNumber();
        int learnedThreshold = (int) Math.ceil((l + f + 1) / 2.0);

        // If the proposer has learned the value, it sends a SATISFIED message to all proposers
        if (messageLog.isLearned(sender, learnMessage, learnedThreshold) && !isSatisfied) {
            log.info("Proposer " + getId() + " sending SATISFIED to all proposer...");
            multicastMessage(new SatisfiedMessage(getId(), learnValue), this.proposerNodeIds);
            isSatisfied = true;
            if (this.proposerTimeoutId != -1) this.clearTimeout(this.proposerTimeoutId);
        }
    }

    /**
     * Handle a LEARN message sent by a Learner replica, received by a Learner replica.
     * @param sender : the nodeId of the sender (a Learner replica)
     * @param learnMessage : the LEARN message with the value and proposal number
     */
    private void handleLearnMessageLearner(String sender, LearnMessage learnMessage) {
        if (this.viewNumber > learnMessage.getValueAndProposalNumber().getNumber()) {
            log.info("Learner " + getId() + " received LEARN message with an outdated proposal number");
            return;
        }

        log.info("Learner " + getId() + " received LEARN from " + sender + " and proposal number " +
                learnMessage.getValueAndProposalNumber().getNumber());
        messageLog.learnerOnLearned(sender, learnMessage);
        int learningThreshold = f + 1;
        if (messageLog.learnerHasLearnedValue(learnMessage.getValueAndProposalNumber(), learningThreshold)) {
             if (this.learnerTimeoutId != -1) this.clearTimeout(this.learnerTimeoutId);
        }
    }

    /**
     * Handle a SATISFIED message received by a Proposer replica.
     * @param sender : the nodeId of the sender (a Proposer replica)
     * @param satisfiedMessage : the SATISFIED message with the value and proposal number
     */
    private void handleSatisfiedMessage(String sender, SatisfiedMessage satisfiedMessage) {
        // Should never happen
        if (this.viewNumber > satisfiedMessage.getValueAndProposalNumber().getNumber()) {
            log.info("Proposer " + getId() + " received SATISFIED message with an outdated proposal number");
            return;
        }

        log.info("Proposer " + getId() + " received SATISFIED from " + sender + " and proposal number " +
                satisfiedMessage.getValueAndProposalNumber().getNumber());
        messageLog.onSatisfied(sender, satisfiedMessage);

        log.info("The number of satisfied proposers is " + messageLog.satisfiedProposersCount());

        int threshold = (int) Math.ceil((p + f + 1) / 2.0);
        if (messageLog.isSatisfied(threshold)) {
            if (isLeader()) {
                this.viewNumber++;
                sendReplyToClient(clientId, proposedValue);
                this.clearAllTimeouts();
                log.info("Finished handling client request");
            }
        } else if (isLeader()) {
            // Set a new timeout for the leader to send a PROPOSE message
            this.setTimeout("leader-timeout",
                    () -> this.multicastMessage(new ProposeMessage(getId(), new Pair(this.viewNumber, this.proposedValue), this.pc), this.nodeIds),
                    Duration.ofMillis(9000));
        }
    }

    /**
     * Handle a QUERY message received by an Acceptor replica.
     * This message is sent after a new leader has been elected.
     * @param sender : the nodeId of the sender (the new leader)
     * @param queryMessage : the QUERY message with the view number and progress certificate
     */
    private void handleQueryMessage(String sender, QueryMessage queryMessage) {
        if (this.viewNumber > queryMessage.getViewNumber()) {
            log.info("Acceptor " + getId() + " received QUERY message with an outdated view number");
            return;
        }
        int quorum = a - f;
        Pair queriedValue = messageLog.onQuery(sender, queryMessage, quorum);

        // Send a REPLY message to the leader.
        // Sign the current accepted value and the current view number.
        sendMessage(new ReplyMessage(queriedValue, true, sender), leaderId);
    }

    /**
     * Handle a PULL message received by a Learner replica. Should send the learned value, if any.
     * @param sender : the nodeId of the sender (a Learner replica)
     * @param pullMessage : the PULL message
     */
    private void handlePullMessage(String sender, PullMessage pullMessage) {
        if (this.viewNumber > pullMessage.getViewNumber()) {
            log.info("Learner " + getId() + " received PULL message with an outdated view number");
            return;
        }

        log.info("Learner " + getId() + " received PULL from " + sender);
        // If this learner has learned a value, send it to the sender
        Pair learnedValue = messageLog.onPull(sender, pullMessage);
        if (learnedValue != null) {
            log.info("Learner " + getId() + " sending LEARN to " + sender);
            sendMessage(new LearnMessage(learnedValue), sender);
        }
    }

    /**
     * Handle a SUSPECT message received by a replica.
     * @param sender : the nodeId of the sender (a replica that suspects the leader)
     * @param suspectMessage : the SUSPECT message with the nodeId of the suspected leader
     */
    private void handleSuspectMessage(String sender, SuspectMessage suspectMessage) {
        if (this.viewNumber > suspectMessage.getViewNumber()) {
            log.info("Replica " + getId() + " received SUSPECT message with an outdated view number");
            return;
        }

        log.info("Replica " + getId() + " received SUSPECT from " + sender);
        int quorum = (int) Math.ceil((p + f + 1) / 2.0);

        if (messageLog.onSuspect(sender, suspectMessage, quorum)) {
            electNewLeader();
        }
    }

    /**
     * Handle a REPLY message received by the leader replica.
     * @param sender : the nodeId of the sender (an Acceptor replica which was queried)
     * @param replyMessage : the REPLY message with the value and round number of the previous round
     */
    public void handleReplyMessage(String sender, ReplyMessage replyMessage) {
        if (this.viewNumber > replyMessage.getValueAndProposalNumber().getNumber()) {
            log.info("Leader " + getId() + " received REPLY message with an outdated proposal number");
            return;
        }

        if (isLeader() && !isRecovered) {
            messageLog.onReply(sender, replyMessage);
            int quorum = a - f;
            // Check if we have enough responses
            if (messageLog.getResponses().size() < quorum) {
                log.info("Leader " + getId() + " did not receive enough responses. Starting recovery protocol...");
                multicastMessage(new QueryMessage(viewNumber), this.acceptorNodeIds);
            } else {
                isRecovered = true;
                this.clearAllTimeouts();
                // Create the progress certificate
                int threshold = (int) Math.ceil((a - f + 1) / 2.0);
                this.pc = new ProgressCertificate(this.viewNumber, messageLog.getResponses());
                // Check if progress certificate vouches for a value
                Optional<byte[]> vouchedValue = this.pc.majorityValue(threshold);

                // If majority value is vouched for, it's the value that wasn't commited last round, propose it again
                if (vouchedValue.isPresent()) {
                    this.proposedValue = vouchedValue.get();
                } else {
                    log.info("Leader " + getId() + " could not find a majority value in the progress certificate");
                    // Proposing a new value
                    this.proposedValue = "123".getBytes();
                }

                ProposeMessage proposeMessage = new ProposeMessage(getId(), new Pair(this.viewNumber, this.proposedValue), this.pc);
                multicastMessage(proposeMessage, this.nodeIds);
                leaderOnStart(proposeMessage);
                onStart();
            }
        }
    }

    /**
     * Handle a VIEW_CHANGE message received by a replica.
     * This is used to update the view number and leader ID sent to all the other replicas
     * after a replica elected a new leader.
     * @param sender : the nodeId of the sender (the replica that elected a new leader)
     * @param viewChangeMessage : the VIEW_CHANGE message with the new view number and leader ID
     */
    private void handleViewChangeMessage(String sender, ViewChangeMessage viewChangeMessage) {
        if (this.viewNumber > viewChangeMessage.getProposalNumber()) {
            log.info("Replica " + getId() + " received VIEW_CHANGE message with an outdated view number");
            return;
        }

        this.viewNumber = viewChangeMessage.getProposalNumber();
        log.info("Before clearing timeouts");
        this.clearAllTimeouts();
        log.info("After clearing timeouts");
        messageLog.acceptViewChange(viewChangeMessage);
        log.info("After accepting");

        // Start recovery protocol
        if (isLeader()) {
            log.info("Leader " + getId() + " received VIEW_CHANGE message and is starting recovery protocol");
            // Send query message to all acceptors until you get a - f replies
            multicastMessage(new QueryMessage(viewNumber), this.acceptorNodeIds);
        } else {
            onStart();
        }
    }

    /**
     * LEADER election mechanism.
     * Elect a new leader based on the view number and node IDs.
     * Once a new leader is elected, notify all other replicas of the new leader through a view change message.
     */
    public void electNewLeader() {
        log.info("Electing a new leader...");
        messageLog.reset();

        // Select the new leader based on the view number and node IDs
        String newLeader = getNewLeader();
        log.info("New leader candidate: " + newLeader);

        isCurrentlyLeader.set(newLeader.equals(getId()));  // This replica is the new leader
        if (isLeader()) isRecovered = false;

        // Notify the other replicas of the new leader
        multicastMessage(new ViewChangeMessage(getId(), this.viewNumber + 1, newLeader), this.getNodeIds());
    }

    private String getNewLeader() {
        // Select the new leader based on the view number and node IDs
        return new ArrayList<>(nodeIds).get((int) (viewNumber % nodeIds.size()));
    }

    /**
     * Reset the replica to a new view number.
     * @param newViewNumber : the new view number
     * @param sender : the nodeId of the sender that triggered the reset
     * @param message : the message that caused the reset
     */
    public void reset(long newViewNumber, String sender, MessagePayload message) {
        // Since we are resetting the replica, previous timeouts should be cleared
        this.clearAllTimeouts();
        // Update the view number
        this.viewNumber = newViewNumber;
        // Clear the message log
        messageLog.resolve();
        messageLog.addMessage(sender, message);
        this.isRecovered = true;
        onStart();
    }

    /** Methods for checking the roles of the replica. **/
    private boolean isAcceptor() {
        return this.roles.contains(Role.ACCEPTOR);
    }

    private boolean isProposer() {
        return this.roles.contains(Role.PROPOSER);
    }

    private boolean isLearner() {
        return this.roles.contains(Role.LEARNER);
    }

    private boolean isLeader() {
        return this.isCurrentlyLeader.get();
    }
}
