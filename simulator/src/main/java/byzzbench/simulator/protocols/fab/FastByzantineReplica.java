package byzzbench.simulator.protocols.fab;

import byzzbench.simulator.Scenario;
import byzzbench.simulator.nodes.LeaderBasedProtocolReplica;
import byzzbench.simulator.protocols.fab.messages.*;
import byzzbench.simulator.state.SerializableLogEntry;
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

/**
 * A replica in the "Fast Byzantine Consensus" protocol.
 */
@Log
@Getter
public class FastByzantineReplica extends LeaderBasedProtocolReplica {
    // The number of replicas in the system with each role.
    private final int p, a, l, f;
    // Timeout until the proposer replica starts suspecting the leader for the lack of progress.
    private final long messageTimeoutDuration;
    @JsonIgnore
    private final Transport transport;
    // Current client ID that the system is communicating with.
    private final String clientId;
    /**
     * The log of received messages for the replica.
     */
    @JsonIgnore
    private final MessageLog messageLog;
    // In the "Fast Byzantine Consensus" protocol, a replica can have one or more roles.
    // The possible roles are : PROPOSER, ACCEPTOR, LEARNER.
    // The required number of replicas with each role is:
    // p ( proposers ) = 3 * f + 1, a ( acceptors ) = 5 * f + 1, l ( learners ) = 3 * f + 1.
    private List<Role> roles = new ArrayList<>();
    // The message round number.
    private long viewNumber;
    private long proposalNumber;
    // The value that the leader replica is proposing - changes each round, depending on the client request.
    private byte[] proposedValue;
    // The progress certificate for the current view.
    private ProgressCertificate pc;
    // Current leader.
    @Setter
    private String leaderId;
    @Setter
    private boolean isCurrentlyLeader;
    // The set of node IDs in the system for each role.
    private SortedSet<String> acceptorNodeIds = new TreeSet<>();
    private SortedSet<String> learnerNodeIds = new TreeSet<>();
    private SortedSet<String> proposerNodeIds = new TreeSet<>();
    private SortedSet<String> nodeIds = new TreeSet<>();
    private boolean isSatisfied;
    // Keep track of the timeouts set by the replica.
    private long learnerTimeoutId = -1;
    private long proposerTimeoutId = -1;
    private long leaderTimeoutId = -1;
    // To be set during the recovery protocol.
    @Setter
    private boolean isRecovered;
    private int forwards;
    private boolean committed;
    private byte[] operation;
    private long viewChangeRequests = 0;

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
            String leaderId,
            String clientId) {
        super(nodeId, scenario, new TotalOrderCommitLog());
        this.roles = roles;
        this.nodeIds = nodeIds;
        this.transport = transport;
        this.messageTimeoutDuration = messageTimeoutDuration;
        this.isCurrentlyLeader = isCurrentlyLeader;
        this.p = p;
        this.a = a;
        this.l = l;
        this.f = f;
        this.acceptorNodeIds = acceptorNodeIds;
        this.learnerNodeIds = learnerNodeIds;
        this.proposerNodeIds = proposerNodeIds;
        this.leaderId = leaderId;
        this.messageLog = new MessageLog(this);
        this.forwards = 0;
        this.clientId = clientId;
    }

    @Override
    public void initialize() {
        log.info("Initializing replica " + getId());
        this.viewNumber = 1;
        this.proposalNumber = 0;
        this.setView(this.viewNumber, this.leaderId);
        onStart();
    }

    public void onStart() {
        log.info("Replica " + getId() + " is starting");
        this.isSatisfied = false;
        messageLog.resolve();
        this.leaderTimeoutId = -1;
        this.proposerTimeoutId = -1;
        this.learnerTimeoutId = -1;
        this.forwards = 0;
        this.committed = false;
        this.viewChangeRequests = 0;

        this.clearAllTimeouts();
        if (isProposer()) proposerOnStart();
        if (isLearner()) learnerOnStart();
    }

    private void leaderOnStart(ProposeMessage proposeMessage) {
        log.info("Replica " + getId() + " is the leader and preparing to send a PROPOSE message to all ACCEPTOR nodes");
        long leaderTimeout = 100;
        this.leaderTimeoutId = this.setTimeout("leader-timeout", () -> {
            log.info("Timer was set");
            this.multicastMessage(
                    proposeMessage,
                    this.acceptorNodeIds
            );

            /* Used for faulty scenario. */
//            this.multicastMessage(new ProposeMessage(this.getId(), new Pair(this.viewNumber, "A".getBytes()), this.pc), new TreeSet<>(List.of("B", "C")));
//            this.multicastMessage(new ProposeMessage(this.getId(), new Pair(this.getViewNumber(), "B".getBytes()), this.pc), new TreeSet<>(List.of("A","D")));
        }, Duration.ofMillis(10));

        this.leaderTimeoutId = this.setTimeout("leader-timeout", () -> {
            log.info("Timer was set");
            this.multicastMessage(
                    proposeMessage,
                    this.acceptorNodeIds
            );
//            this.multicastMessage(new ProposeMessage(this.getId(), new Pair(this.viewNumber, "A".getBytes()), this.pc), new TreeSet<>(List.of("B", "C")));
//            this.multicastMessage(new ProposeMessage(this.getId(), new Pair(this.getViewNumber(), "B".getBytes()), this.pc), new TreeSet<>(List.of("A","D")));
        }, Duration.ofMillis(100));
//
        this.leaderTimeoutId = this.setTimeout("leader-timeout", () -> {
            log.info("Timer was set");
            this.multicastMessage(
                    proposeMessage,
                    this.acceptorNodeIds
            );
//            this.multicastMessage(new ProposeMessage(this.getId(), new Pair(this.viewNumber, "A".getBytes()), this.pc), new TreeSet<>(List.of("B", "C")));
//            this.multicastMessage(new ProposeMessage(this.getId(), new Pair(this.getViewNumber(), "B".getBytes()), this.pc), new TreeSet<>(List.of("A","D")));
        }, Duration.ofMillis(300));

        this.leaderTimeoutId = this.setTimeout("leader-timeout", () -> {
            log.info("Timer was set");
            this.multicastMessage(
                    proposeMessage,
                    this.acceptorNodeIds
            );
//            this.multicastMessage(new ProposeMessage(this.getId(), new Pair(this.viewNumber, "A".getBytes()), this.pc), new TreeSet<>(List.of("B", "C")));
//            this.multicastMessage(new ProposeMessage(this.getId(), new Pair(this.getViewNumber(), "B".getBytes()), this.pc), new TreeSet<>(List.of("A","D")));
        }, Duration.ofMillis(400));
    }

    /**
     * PROPOSER LOGIC
     * The PROPOSER starts by waiting to learn the accepted value from the ACCEPTOR nodes.
     * If the PROPOSER does not receive enough LEARN messages, it suspects the leader due to the lack of progress.
     */
    private void proposerOnStart() {
        int learnedThreshold = (int) Math.ceil((l + f + 1) / 2.0);

        this.proposerTimeoutId = this.setTimeout(
                "proposer-timeout",
                () -> {
                    if (!messageLog.proposerHasLearned(learnedThreshold)) {
                        log.warning("Leader suspected by proposer " + getId());
                        broadcastMessageIncludingSelf(new SuspectMessage(getId(), this.leaderId, this.viewNumber));
                    }
                }, Duration.ofMillis(messageTimeoutDuration));
    }

    /**
     * Learner replica starts by awaiting to learn a value.
     * If it has not learned any value after waiting, send a PULL message to all other LEARNER replicas.
     * The PULL message is used to request the learned value from other LEARNER replicas.
     */
    private void learnerOnStart() {
        long learnerTimeout = 200;
        TreeSet<String> learners = new TreeSet<>(this.learnerNodeIds);
        learners.remove(getId());

        if (messageLog.getLearnedValue() == null) {
            log.info("Learner " + getId() + " has not learned a value. Sending PULL message...");
            this.learnerTimeoutId = this.setTimeout(
                    "learner-timeout",
                    () -> multicastMessage(new PullMessage(this.viewNumber), learners),
                    Duration.ofMillis(300));
        }
    }

    /**
     * Handle a client request received by the replica.
     *
     * @param clientId the ID of the client
     * @param request  the request payload
     */
    public void handleClientRequest(String clientId, Serializable request) throws UnsupportedOperationException {
        // If the client request is not send to the leader, forward it to the leader.
        if (!isLeader()) {
            return;
        }

        // NEW-VIEW message is sent to all replicas to move to the next view number.
        if (proposedValue != null) {
            log.info("Finished handling client request");
            reset(this.viewNumber, getId(), new ForwardClientRequest(clientId, request.toString()));
            this.broadcastMessage(new NewViewMessage(this.viewNumber));
        }

        // Sending PROPOSE message to all ACCEPTOR nodes.
        proposedValue = request.toString().getBytes();
        this.pc = null;
        ProposeMessage proposeMessage = new ProposeMessage(getId(), new Pair(this.viewNumber, this.proposedValue), this.pc);
        this.multicastMessage(proposeMessage, this.acceptorNodeIds);

        /* Used in faulty scenario. */
//        proposedValue = "A".getBytes();
//        this.multicastMessage(new ProposeMessage(this.getId(), new Pair(this.viewNumber, "A".getBytes()), this.pc), new TreeSet<>(List.of("B", "C")));
//        proposedValue ="B".getBytes();
//        this.multicastMessage(new ProposeMessage(this.getId(), new Pair(this.getViewNumber(), "B".getBytes()), this.pc), new TreeSet<>(List.of("A", "D")));
        leaderOnStart(proposeMessage);
        messageLog.resolveClientRequest(clientId, request);
    }

    @Override
    public void handleMessage(String sender, MessagePayload message) throws UnsupportedOperationException {
        log.info("Replica " + getId() + " received a message from " + sender);
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
            case NewViewMessage newViewMessage -> handleNewViewMessage(sender, newViewMessage);
            case DefaultClientRequestPayload clientRequest -> handleClientRequest(sender, clientRequest.getOperation());
            case ForwardClientRequest forwardClientRequest -> {
                if (!isLeader()) handleClientRequest(sender, forwardClientRequest.getOperation());
                if (isLeader()) handleForwardClientRequest(sender, forwardClientRequest);
            }
            case NewViewChangeMessage newViewChangeMessage -> handleNewViewChangeMessage(sender, newViewChangeMessage);
            case null, default -> {
                String messageType = message == null ? "null" : message.getType();
                throw new UnsupportedOperationException("Unknown message type: " + messageType);
            }
        }
    }

    private void handleForwardClientRequest(String sender, ForwardClientRequest forwardClientRequest) {
        this.forwards++;
        log.info("Replica " + getId() + " received a forwarded client request from " + sender);

        // Exclude leader.
        if (this.forwards == this.nodeIds.size() - 1) {
            if (committed) {
                log.info("Client ID: " + this.clientId);
                this.sendReplyToClient(clientId, 0, proposedValue);
                log.info("Finished handling client request");
                this.broadcastMessage(new NewViewMessage(this.viewNumber + 1));
                reset(this.viewNumber, getId(), forwardClientRequest);
            } else {
                this.setTimeout("retry", () -> handleForwardClientRequest(sender, forwardClientRequest), Duration.ofMillis(200));
            }
        }
    }

    /**
     * Handle a PROPOSE message send by a replica with Proposer role, who is the leader, received by all Acceptor replicas.
     *
     * @param sender         : the nodeId of the sender (the current leader)
     * @param proposeMessage : the PROPOSE message with the proposed value and round number
     */
    private void handleProposeMessage(String sender, ProposeMessage proposeMessage) {
        log.info("Acceptor " + getId() + " received PROPOSE from " + sender + " and proposal number " + proposeMessage.getValueAndProposalNumber().getNumber());

        long proposalNumber = proposeMessage.getValueAndProposalNumber().getNumber();
        // The protocol is in a new view, ignore past proposals
        int vouchingThreshold = (int) Math.ceil((a - f + 1) / 2.0);
        operation = proposeMessage.getValueAndProposalNumber().getValue();
        if (messageLog.onPropose(sender, proposeMessage, vouchingThreshold)) {
            log.info("Acceptor " + getId() + " accepted proposal with value " + new String(proposeMessage.getValueAndProposalNumber().getValue()));
            multicastMessage(new AcceptMessage(getId(), proposeMessage.getValueAndProposalNumber()), this.learnerNodeIds);
        }
    }

    /**
     * Handle an ACCEPT message sent by an Acceptor replica, received by a Learner replica.
     *
     * @param sender        : the nodeId of the sender (an Acceptor replica)
     * @param acceptMessage : the ACCEPT message with the value and proposal number
     */
    private void handleAcceptMessage(String sender, AcceptMessage acceptMessage) {
        long proposalNumber = acceptMessage.getValueAndProposalNumber().getNumber();
        if (this.viewNumber > proposalNumber) {
            log.info("Learner " + getId() + " received ACCEPT message with an outdated proposal number");
            return;
        }

        log.info("Learner " + getId() + " received ACCEPT from " + sender + " and proposal number " + proposalNumber);
        int acceptedThreshold = (int) Math.ceil((a + (3 * f) + 1) / 2.0);
        boolean isAccepted = messageLog.onAccept(sender, acceptMessage, acceptedThreshold);
        operation = acceptMessage.getValueAndProposalNumber().getValue();
        if (isAccepted) {
            log.info("Learner " + getId() + " accepted the proposal with value " + new String(acceptMessage.getValueAndProposalNumber().getValue()));
            Pair acceptValue = acceptMessage.getValueAndProposalNumber();
            // Commit the operation
            if (!committed) {
                log.info("Learner " + getId() + " has commited the operation " + new String(acceptValue.getValue()));
                this.commitOperation(new SerializableLogEntry(acceptValue.getValue()));
                committed = true;

                log.info("Learner " + getId() + " sending reply to client...");
                this.sendReplyToClient(clientId, 0, acceptValue.getValue());
            }

            multicastMessage(new LearnMessage(acceptValue), this.proposerNodeIds);
            if (this.learnerTimeoutId != -1) this.clearTimeout(this.learnerTimeoutId);
        }
    }

    /**
     * Handle a LEARN message sent by a Proposer replica, received by a Proposer replica.
     *
     * @param sender       : the nodeId of the sender (a Learner replica)
     * @param learnMessage : the LEARN message with the value and proposal number
     */
    private void handleLearnMessageProposer(String sender, LearnMessage learnMessage) {
        long proposalNumber = learnMessage.getValueAndProposalNumber().getNumber();
        // Should never be the case for this
        if (this.viewNumber > proposalNumber) {
            log.info("Proposer " + getId() + " received LEARN message with an outdated proposal number");
            return;
        }

        log.info("Proposer " + getId() + " received LEARN from " + sender + " and proposal number " + proposalNumber);
        Pair learnValue = learnMessage.getValueAndProposalNumber();
        int learnedThreshold = (int) Math.ceil((l + f + 1) / 2.0);
        operation = learnValue.getValue();
        // If the proposer has learned the value, it sends a SATISFIED message to all proposers
        if (messageLog.isLearned(sender, learnMessage, learnedThreshold) && !isSatisfied) {
            // Commit the operation
//            if (!committed) {
//                log.info("Proposer " + getId() + " has commited the operation " + new String(learnValue.getValue()));
//                this.commitOperation(new SerializableLogEntry(learnValue.getValue()));
//                committed = true;
//
//                log.info("Proposer " + getId() + " sending reply to client...");
//                if (!isLeader()) this.sendReplyToClient(clientId, learnValue.getValue());
//            }

            log.info("Proposer " + getId() + " sending SATISFIED to all proposer...");
            multicastMessage(new SatisfiedMessage(getId(), learnValue), this.proposerNodeIds);
            isSatisfied = true;
            if (this.proposerTimeoutId != -1) this.clearTimeout(this.proposerTimeoutId);
        }
    }

    /**
     * Handle a LEARN message sent by a Learner replica, received by a Learner replica.
     *
     * @param sender       : the nodeId of the sender (a Learner replica)
     * @param learnMessage : the LEARN message with the value and proposal number
     */
    private void handleLearnMessageLearner(String sender, LearnMessage learnMessage) {
        long proposalNumber = learnMessage.getValueAndProposalNumber().getNumber();

        if (this.viewNumber > proposalNumber) {
            log.info("Learner " + getId() + " received LEARN message with an outdated proposal number");
            return;
        }

        log.info("Learner " + getId() + " received LEARN from " + sender + " and proposal number " + proposalNumber);
        messageLog.learnerOnLearned(sender, learnMessage);
        int learningThreshold = f + 1;
        operation = learnMessage.getValueAndProposalNumber().getValue();
        if (messageLog.learnerHasLearnedValue(learnMessage.getValueAndProposalNumber(), learningThreshold)) {
            if (!committed) {
                log.info("Learner " + getId() + " has commited the operation " + new String(learnMessage.getValueAndProposalNumber().getValue()));
                this.commitOperation(new SerializableLogEntry(learnMessage.getValueAndProposalNumber().getValue()));
                committed = true;

                log.info("Learner " + getId() + " sending reply to client...");
                this.sendReplyToClient(clientId, 0, learnMessage.getValueAndProposalNumber().getValue());

            }

            if (this.learnerTimeoutId != -1) this.clearTimeout(this.learnerTimeoutId);
        } else {
            log.info("Learner " + getId() + " has not learned the value yet");
            this.learnerTimeoutId = this.setTimeout(
                    "learner-timeout",
                    () -> multicastMessage(new PullMessage(this.viewNumber), this.learnerNodeIds),
                    Duration.ofMillis(300));
        }
    }

    /**
     * Handle a SATISFIED message received by a Proposer replica.
     *
     * @param sender           : the nodeId of the sender (a Proposer replica)
     * @param satisfiedMessage : the SATISFIED message with the value and proposal number
     */
    private void handleSatisfiedMessage(String sender, SatisfiedMessage satisfiedMessage) {
        long proposalNumber = satisfiedMessage.getValueAndProposalNumber().getNumber();
        if (this.viewNumber > proposalNumber) {
            log.info("Proposer " + getId() + " received SATISFIED message with an outdated proposal number");
            return;
        }

        log.info("Proposer " + getId() + " received SATISFIED from " + sender + " and proposal number " + proposalNumber);
        messageLog.onSatisfied(sender, satisfiedMessage);

        log.info("The number of satisfied proposers is " + messageLog.satisfiedProposersCount());

        int threshold = (int) Math.ceil((p + f + 1) / 2.0);
        operation = satisfiedMessage.getValueAndProposalNumber().getValue();
        log.info("Threshold is " + threshold);
        log.info("Satisfied proposers count is " + messageLog.satisfiedProposersCount());
        log.info("Is satisfied? " + messageLog.isSatisfied(threshold));

        if (isLeader()) {
            log.info("Replica " + getId() + " is the leader and received SATISFIED message");

            if (messageLog.isSatisfied(threshold) && committed) {
                if (leaderTimeoutId != -1) this.clearTimeout(leaderTimeoutId);
            } else {
                // Set a new timeout for the leader to send a PROPOSE message
                this.leaderTimeoutId = this.setTimeout("leader-timeout",
                        () -> this.multicastMessage(new ProposeMessage(getId(), new Pair(this.viewNumber, this.proposedValue), this.pc), this.acceptorNodeIds),
                        Duration.ofMillis(100));
            }
        }
    }

    /**
     * Handle a QUERY message received by an Acceptor replica.
     * This message is sent after a new leader has been elected.
     *
     * @param sender       : the nodeId of the sender (the new leader)
     * @param queryMessage : the QUERY message with the view number and progress certificate
     */
    private void handleQueryMessage(String sender, QueryMessage queryMessage) {
        long proposalNumber = queryMessage.getViewNumber();

        if (this.viewNumber > proposalNumber) {
            log.info("Acceptor " + getId() + " received QUERY message with an outdated view number");
            return;
        }

        log.info("Acceptor " + getId() + " received QUERY from " + sender + " and view number " + this.viewNumber);
        int quorum = a - f;
        Pair queriedValue = messageLog.onQuery(sender, queryMessage, quorum);

        // Send a REPLY message to the leader.
        // Sign the current accepted value and the current view number.
        if (queriedValue != null) {
            log.info("Acceptor " + getId() + " sending REPLY to " + sender + " with value " + new String(queriedValue.getValue()));
            log.info("Leader is " + leaderId);
            sendMessage(new ReplyMessage(messageLog.getAcceptedProposal(), true, sender, this.clientId), leaderId);
        } else {
            log.info("Acceptor " + getId() + " did not have a value to reply with");
        }
    }

    /**
     * Handle a PULL message received by a Learner replica. Should send the learned value, if any.
     *
     * @param sender      : the nodeId of the sender (a Learner replica)
     * @param pullMessage : the PULL message
     */
    private void handlePullMessage(String sender, PullMessage pullMessage) {
        long proposalNumber = pullMessage.getViewNumber();
        if (this.viewNumber != proposalNumber) {
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
     *
     * @param sender         : the nodeId of the sender (a replica that suspects the leader)
     * @param suspectMessage : the SUSPECT message with the nodeId of the suspected leader
     */
    private void handleSuspectMessage(String sender, SuspectMessage suspectMessage) {
        long proposalNumber = suspectMessage.getViewNumber();
        if (this.viewNumber > proposalNumber) {
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
     *
     * @param sender       : the nodeId of the sender (an Acceptor replica which was queried)
     * @param replyMessage : the REPLY message with the value and round number of the previous round
     */
    public void handleReplyMessage(String sender, ReplyMessage replyMessage) {
        long proposalNumber = replyMessage.getValueAndProposalNumber().getNumber();
//        if (this.viewNumber > proposalNumber) {
//            log.info("Leader " + getId() + " received REPLY message with an outdated proposal number");
//            return;
//        }

        if (isLeader() && !isRecovered) {
            messageLog.onReply(sender, replyMessage);

            int quorum = a - f;
            // Check if we have enough responses
            if (messageLog.getResponses().size() < quorum) {
                log.info("Leader " + getId() + " did not receive enough responses yet.");
//                multicastMessage(new QueryMessage(viewNumber), this.acceptorNodeIds);
            } else {
                isRecovered = true;
                // Create the progress certificate
                int threshold = (int) Math.ceil((a - f + 1) / 2.0);
                this.pc = new ProgressCertificate(this.viewNumber, messageLog.getResponses());
                // Check if progress certificate vouches for a value
                Optional<byte[]> vouchedValue = this.pc.majorityValue(threshold);

                // If majority value is vouched for, it's the value that wasn't committed last round, propose it again
                if (vouchedValue.isPresent()) {
                    log.info("Leader " + getId() + " found a majority value in the progress certificate");
                    this.proposedValue = vouchedValue.get();
                    ProposeMessage proposeMessage = new ProposeMessage(getId(), new Pair(this.viewNumber, this.proposedValue), this.pc);
                    multicastMessage(proposeMessage, this.acceptorNodeIds);
                    leaderOnStart(proposeMessage);
                } else {
                    log.info("Leader " + getId() + " could not find a majority value in the progress certificate");
                }
            }
        }
    }

    /**
     * Handle a VIEW_CHANGE message received by a replica.
     * This is used to update the view number and leader ID sent to all the other replicas
     * after a replica elected a new leader.
     *
     * @param sender            : the nodeId of the sender (the replica that elected a new leader)
     * @param viewChangeMessage : the VIEW_CHANGE message with the new view number and leader ID
     */
    private void handleViewChangeMessage(String sender, ViewChangeMessage viewChangeMessage) {
        long proposalNumber = viewChangeMessage.getProposalNumber();
        if (this.viewNumber > proposalNumber) {
            log.info("Replica " + getId() + " received VIEW_CHANGE message with an outdated view number");
            return;
        }

        if (viewChangeMessage.getNewLeaderId().equals(getId())) {
            viewChangeRequests++;
        }

        // Start recovery protocol
        if (viewChangeMessage.getNewLeaderId().equals(getId()) && viewChangeRequests >= f + 1) {
            log.info("View change number: " + proposalNumber);
            this.viewNumber = proposalNumber;
            this.leaderId = viewChangeMessage.getNewLeaderId();
            this.setView(this.viewNumber, this.leaderId);
            this.clearAllTimeouts();
            messageLog.acceptViewChange(viewChangeMessage);
            this.isCurrentlyLeader = true;

            this.isSatisfied = false;
            messageLog.reset();
            this.leaderTimeoutId = -1;
            this.proposerTimeoutId = -1;
            this.learnerTimeoutId = -1;
            this.forwards = 0;
//            this.committed = false;
            this.viewChangeRequests = 0;

            this.clearAllTimeouts();
            if (isProposer()) proposerOnStart();
            if (isLearner()) learnerOnStart();

            log.info("Leader " + getId() + " received VIEW_CHANGE message and is starting recovery protocol");
            this.isRecovered = false;
            // Send query message to all acceptors until you get a - f replies
            log.info("Leader in view change message: " + viewChangeMessage.getNewLeaderId());
            broadcastMessage(new NewViewChangeMessage(this.viewNumber, this.leaderId));
            multicastMessage(new QueryMessage(this.viewNumber), this.acceptorNodeIds);
        }
    }

    private void handleNewViewMessage(String sender, NewViewMessage newViewMessage) {
        log.info("Replica " + getId() + " received NEW_VIEW message from " + sender);
        log.info("Progressing to : view " + newViewMessage.getViewNumber());
        long proposalNumber = newViewMessage.getViewNumber();
        if (this.viewNumber >= proposalNumber) {
            log.info("Replica " + getId() + " received NEW_VIEW message with an outdated view number");
            return;
        }

        this.viewNumber = proposalNumber;
        this.setView(this.viewNumber, this.leaderId);
        this.clearAllTimeouts();

        onStart();
    }

    private void handleNewViewChangeMessage(String sender, NewViewChangeMessage newViewMessage) {
        log.info("Replica " + getId() + " received NEW_VIEW_CHANGE message from " + sender);
        long proposalNumber = newViewMessage.getViewNumber();
        if (this.viewNumber >= proposalNumber) {
            log.info("Replica " + getId() + " received NEW_VIEW_CHANGE message with an outdated view number");
            return;
        }

        log.info("Replica " + getId() + " is moving to the next view number: view " + newViewMessage.getViewNumber());
        this.viewNumber = proposalNumber;
        this.leaderId = newViewMessage.getNewLeaderId();
        this.setView(this.viewNumber, newViewMessage.getNewLeaderId());
        this.clearAllTimeouts();
        this.isCurrentlyLeader = this.getId().equals(this.leaderId);
        log.info("New leader is " + newViewMessage.getNewLeaderId());
        log.info("Restarting the replica...");

        this.isSatisfied = false;
        messageLog.reset();
        this.leaderTimeoutId = -1;
        this.proposerTimeoutId = -1;
        this.learnerTimeoutId = -1;
        this.forwards = 0;
        this.viewChangeRequests = 0;

        if (isProposer()) proposerOnStart();
        if (isLearner()) learnerOnStart();
    }

    /**
     * Leader election mechanism.
     * Elect a new leader based on the view number and node IDs.
     * Once a new leader is elected, notify all other replicas of the new leader through a view change message.
     */
    public void electNewLeader() {
        log.info("Replica " + getId() + " is electing a new leader");

        // Select the new leader based on the view number and node IDs
        String newLeader = getNewLeader();
        log.info("New leader candidate: " + newLeader);
//        this.isCurrentlyLeader = newLeader.equals(getId());  // This replica is the new leader
//        if (isLeader()) isRecovered = false;

        // Notify the other replicas of the view change request
        multicastMessage(new ViewChangeMessage(getId(), this.viewNumber + 1, newLeader), this.getNodeIds());
    }

    private String getNewLeader() {
        // Select the new leader based on the view number and node IDs
        return new ArrayList<>(proposerNodeIds).get((int) ((viewNumber + 1) % proposerNodeIds.size()));
    }

    /**
     * Move replica to the next view number and reset the replica.
     *
     * @param newViewNumber : the new view number
     * @param sender        : the nodeId of the sender that triggered the reset
     * @param message       : the message that caused the reset
     */
    public void reset(long newViewNumber, String sender, MessagePayload message) {
        // Since we are resetting the replica, previous timeouts should be cleared
        committed = false;
        this.clearAllTimeouts();
        // Update the view number
        this.viewNumber++;
        this.setView(this.viewNumber, this.leaderId);
        // Clear the message log
        messageLog.resolve();
        messageLog.addMessage(sender, message);
        this.isRecovered = true;
        this.isSatisfied = false;
        this.forwards = 0;
        onStart();
    }

    /**
     * Methods for checking the roles of the replica.
     **/
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
        return this.isCurrentlyLeader;
    }
}
