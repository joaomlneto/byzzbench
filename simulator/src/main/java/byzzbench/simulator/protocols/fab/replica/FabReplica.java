package byzzbench.simulator.protocols.fab.replica;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.protocols.fab.ProgressCertificate;
import byzzbench.simulator.protocols.fab.messages.*;
import byzzbench.simulator.protocols.fab.MessageLog;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A replica in the FAB protocol.
 */
@Log
@Getter
public class FabReplica extends LeaderBasedProtocolReplica {
    private final List<FabRole> roles;
    // The number of replicas with each role
    // p = number of proposers, a = number of acceptors, l = number of learners, f = number of faulty nodes
    private final int p, a, l, f;
    private long viewNumber;
    private byte[] proposedValue;
    private ProgressCertificate pc;
    private final long messageTimeoutDuration;
    private int proposalNumber;
    @Setter
    private String leaderId;
    private final AtomicBoolean isCurrentlyLeader = new AtomicBoolean(false);

    private final SortedSet<String> acceptorNodeIds;
    private final SortedSet<String> learnerNodeIds;
    private final SortedSet<String> proposerNodeIds;
    private final SortedSet<String> nodeIds;

    private final Transport transport;
    private ExecutorService executor = Executors.newCachedThreadPool();

    private String clientId;
    /**
     * The log of received messages for the replica.
     */
    @JsonIgnore
    private MessageLog messageLog;

    public FabReplica(
            String nodeId,
            SortedSet<String> nodeIds,
            Transport transport,
            Scenario scenario,
            List<FabRole> roles,
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
    }

    public void onStart() {
        log.info("Replica " + getId() + " is starting");

        CompletableFuture<Void> proposerTask = isProposer() ? proposerOnStart() : CompletableFuture.completedFuture(null);
        CompletableFuture<Void> learnerTask = isLearner() ? learnerOnStart() : CompletableFuture.completedFuture(null);

        CompletableFuture.allOf(proposerTask, learnerTask).thenRun(() ->
                log.info("Replica " + getId() + " finished starting.")
        ).exceptionally(e -> {
            log.severe("Replica " + getId() + " failed to start: " + e.getMessage());
            return null;
        });
    }

    /**
     * LEADER LOGIC
     * The LEADER starts by sending a PROPOSE message to all ACCEPTOR nodes. It keeps sending the message until
     * the threshold - ceil((p + f + 1) / 2) - is reached.
     */
    private CompletableFuture<Void> leaderOnStart(ProposeMessage proposeMessage) {
        log.info("Replica " + getId() + " is the leader and preparing to send a PROPOSE message to all ACCEPTOR nodes");
        int threshold = (int) Math.ceil((p + f + 1) / 2.0);
        SortedSet<String> acceptorReceipts = new TreeSet<>(List.of("A", "B", "C", "D", "E", "F"));

        return CompletableFuture.runAsync(() -> {
            while (!messageLog.isSatisfied(threshold)) {
                long id = this.setTimeout("leader-timeout", () -> {
                    multicastMessage(proposeMessage, acceptorReceipts);
                }, Duration.ofMillis(4000));

                this.clearTimeout(id);
            }
        });
    }

    /**
     * PROPOSER LOGIC
     * The PROPOSER starts by waiting to learn the accepted value from the ACCEPTOR nodes.
     * If the PROPOSER does not receive enough LEARN messages, it suspects the leader due to the lack of progress.
     */
    private CompletableFuture<Void> proposerOnStart() {
        int learnedThreshold = (int) Math.ceil((l + f + 1) / 2.0);
        AtomicLong id = new AtomicLong();

        return CompletableFuture.runAsync(() -> {
             id.set(this.setTimeout("proposer-timeout", () -> {
                 if (!messageLog.proposerHasLearned(learnedThreshold)) {
//                    log.warning("Leader suspected by proposer " + getId());
                     broadcastMessage(new SuspectMessage(getId(), this.leaderId, this.viewNumber));
                 }
             }, Duration.ofMillis(900000)));

            this.clearTimeout(id.get());
        }, executor);
    }

    /**
     * LEARNER LOGIC
     * The LEARNER, if it has not learned a value, sends a PULL message to all LEARNER nodes.
     */
    private CompletableFuture<Void> learnerOnStart() {
        AtomicLong id = new AtomicLong(0L);
        //                log.info("Learner " + getId() + " has not learned a value. Sending PULL message...");
        // Clear the timeout if the learner has learned a value

        return CompletableFuture.runAsync(() -> {
            if (messageLog.getLearnedValue() == null) {
//                log.info("Learner " + getId() + " has not learned a value. Sending PULL message...");
//                id.set(pullLearnedValue());
            }

//            this.clearTimeout(id.get());
            // Clear the timeout if the learner has learned a value
            log.info("Learner " + getId() + " ¬learned value: " + Arrays.toString(messageLog.getLearnedValue().getValue()));
        }, executor);
    }

    public long pullLearnedValue() {
        return this.setTimeout("learner-timeout", () -> {
//            log.info("Learner " + getId() + " sending PULL to all learner nodes...");
            multicastMessage(new PullMessage(this.viewNumber), this.learnerNodeIds);
        }, Duration.ofMillis(4000));
    }

    @Override
    public void handleClientRequest(String clientId, Serializable request) throws UnsupportedOperationException {
        if (!isLeader()) {
            log.info("Replica " + getId() + " is not the leader. Forwarding client request to leader.");
            return;
        }

        /* Sending the PROPOSE message for the first time. */
        if (pc == null) { proposedValue = request.toString().getBytes(); }
        this.clientId = clientId;
        ProposeMessage proposeMessage = new ProposeMessage(this.getId(), new Pair(this.viewNumber, this.proposedValue), this.pc);
        this.multicastMessage(proposeMessage, this.nodeIds);

//        onStart();
        /* Starting the leader task. */
//        this.onStart().thenRun(() -> log.info("Replica " + getId() + " finished starting."));
        leaderOnStart(proposeMessage).thenRun(() -> {
            log.info("Finished handling client request");
            multicastMessage(new NewViewMessage(viewNumber + 1), this.nodeIds);
            messageLog.resolveClientRequest(clientId, request);
            this.sendReplyToClient(clientId, proposedValue);
        }).exceptionally(ex -> {
            // Handle any exceptions that might occur during the leader task or subsequent operations
            log.info("Error occurred while handling client request: " + ex.getMessage());
            return null;
        });
    }

    @Override
    public void handleMessage(String sender, MessagePayload message) throws UnsupportedOperationException {
//        log.info(String.format("Replica %s received a message from %s: %s", getId(), sender, message));
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
        int vouchingThreshold = (int) Math.ceil((a - f + 1) / 2.0);

        if (messageLog.onPropose(sender, proposeMessage, vouchingThreshold)) {
            log.info("Acceptor " + getId() + " accepted proposal with value " + new String(proposeMessage.getValueAndProposalNumber().getValue()));
            multicastMessage(new AcceptMessage(getId(), proposeMessage.getValueAndProposalNumber()), this.learnerNodeIds);
        } else {
            log.info("Acceptor " + getId() + " ignoring PROPOSE message with round number " +  proposeMessage.getValueAndProposalNumber().getNumber());
        }
    }

    /**
     * Handle an ACCEPT message sent by an Acceptor replica, received by a Learner replica.
     * @param sender : the nodeId of the sender (an Acceptor replica)
     * @param acceptMessage : the ACCEPT message with the value and proposal number
     */
    private void handleAcceptMessage(String sender, AcceptMessage acceptMessage) {
        log.info("Learner " + getId() + " received ACCEPT from " + sender + " and proposal number " + acceptMessage.getValueAndProposalNumber().getNumber());
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
        log.info("Proposer " + getId() + " received LEARN from " + sender + " and proposal number " + learnMessage.getValueAndProposalNumber().getNumber());
        Pair learnValue = learnMessage.getValueAndProposalNumber();
        int learnedThreshold = (int) Math.ceil((l + f + 1) / 2.0);

        if (messageLog.isLearned(sender, learnMessage, learnedThreshold)) {
            // Send SATISFIED message to all PROPOSER nodes
            log.info("Proposer " + getId() + " sending SATISFIED to all proposer...");
            multicastMessage(new SatisfiedMessage(getId(), learnValue), this.proposerNodeIds);
        }
    }

    /**
     * Handle a LEARN message sent by a Learner replica, received by a Learner replica.
     * @param sender : the nodeId of the sender (a Learner replica)
     * @param learnMessage : the LEARN message with the value and proposal number
     */
    private void handleLearnMessageLearner(String sender, LearnMessage learnMessage) {
        log.info("Learner " + getId() + " received LEARN from " + sender + " and proposal number " + learnMessage.getValueAndProposalNumber().getNumber());
        messageLog.learnerOnLearned(sender, learnMessage);
        int learningThreshold = f + 1;
        messageLog.learnerHasLearnedValue(learnMessage.getValueAndProposalNumber(), learningThreshold);
    }

    /**
     * Handle a SATISFIED message received by a Proposer replica.
     * @param sender : the nodeId of the sender (a Proposer replica)
     * @param satisfiedMessage : the SATISFIED message with the value and proposal number
     */
    private void handleSatisfiedMessage(String sender, SatisfiedMessage satisfiedMessage) {
        log.info("Proposer " + getId() + " received SATISFIED from " + sender + " and proposal number " + satisfiedMessage.getValueAndProposalNumber().getNumber());
        messageLog.onSatisfied(sender, satisfiedMessage);
        log.info("The number of satisfied proposers is " + messageLog.satisfiedProposersCount());
    }

    /**
     * Handle a QUERY message received by an Acceptor replica.
     * This message is sent after a new leader has been elected.
     * @param sender : the nodeId of the sender (the new leader)
     * @param queryMessage : the QUERY message with the view number and progress certificate
     */
    private void handleQueryMessage(String sender, QueryMessage queryMessage) {
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
        messageLog.onReply(sender, replyMessage);
    }

    /**
     * Handle a VIEW_CHANGE message received by a replica.
     * This is used to update the view number and leader ID sent to all the other replicas
     * after a replica elected a new leader.
     * @param sender : the nodeId of the sender (the replica that elected a new leader)
     * @param viewChangeMessage : the VIEW_CHANGE message with the new view number and leader ID
     */
    private void handleViewChangeMessage(String sender, ViewChangeMessage viewChangeMessage) {
        messageLog.acceptViewChange(viewChangeMessage);
//        this.clearAllTimeouts();

        // Start recovery protocol
        int quorum = a - f;
        if (isLeader()) {
            // Send query message to all acceptors until you get a - f replies
            long id;
            while (messageLog.getResponses().size() < quorum) {
                id = this.setTimeout("query-timeout", () -> {
                    multicastMessage(new QueryMessage(viewNumber), this.acceptorNodeIds);
                }, Duration.ofMillis(4000));

                this.clearTimeout(id);
            }

            int threshold = (int) Math.ceil((a - f + 1) / 2.0);
            this.pc = new ProgressCertificate(this.viewNumber, messageLog.getResponses());
            Optional<byte[]> vouchedValue = this.pc.majorityValue(threshold);

            if (vouchedValue.isPresent()) {
                this.proposedValue = vouchedValue.get();
                this.handleClientRequest("client", new String(vouchedValue.get()));
            } else {
                log.info("Leader " + getId() + " could not find a majority value in the progress certificate");
                // Proposing a new value
                this.proposedValue = clientId.getBytes();
            }
        }
    }

    private void handleNewViewMessage(String sender, NewViewMessage newViewMessage) {
        messageLog.acceptNewView(newViewMessage);
        executor.shutdown();
        executor = Executors.newCachedThreadPool();
//        this.clearAllTimeouts();
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
        this.viewNumber++;
        this.leaderId = newLeader;  // Set the new leader

        isCurrentlyLeader.set(newLeader.equals(getId()));  // This replica is the new leader

        // Notify the other replicas of the new leader
        multicastMessage(new ViewChangeMessage(getId(), viewNumber, newLeader), this.getNodeIds());
    }

    private String getNewLeader() {
        // Select the new leader based on the view number and node IDs
        return new ArrayList<>(nodeIds).get((int) (viewNumber % nodeIds.size()));
    }

    /** Methods for checking the roles of the replica. **/
    private boolean isAcceptor() {
        return this.roles.contains(FabRole.ACCEPTOR);
    }

    private boolean isProposer() {
        return this.roles.contains(FabRole.PROPOSER);
    }

    private boolean isLearner() {
        return this.roles.contains(FabRole.LEARNER);
    }

    private boolean isLeader() {
        return this.isCurrentlyLeader.get();
    }
}
