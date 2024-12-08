package byzzbench.simulator.protocols.fab.replica;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.Scenario;
import byzzbench.simulator.protocols.fab.ProgressCertificate;
import byzzbench.simulator.protocols.fab.SignedResponse;
import byzzbench.simulator.protocols.fab.messages.*;
import byzzbench.simulator.protocols.fab.MessageLog;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.DefaultClientRequestPayload;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A replica in the FAB protocol.
 */
@Log
@Getter
public class FabReplica extends LeaderBasedProtocolReplica {
    List<FabRole> roles;

    // The number of replicas with each role
    private final int numProposers;
    private final int numAcceptors;
    private final int numLearners;
    private final int numFaulty;

    // Learner role

    // Proposer role
    private long viewNumber;
    private byte[] proposedValue;
    private ProgressCertificate pc;

    private final AtomicBoolean isCurrentlyLeader = new AtomicBoolean(false);

    private final long messageTimeoutDuration;

    private String leaderId;

    private final SortedSet<String> acceptorNodeIds;
    private final SortedSet<String> learnerNodeIds;
    private final SortedSet<String> proposerNodeIds;

    private final SortedSet<String> nodeIds;
    private final Transport transport;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    private String clientId;

    /**
     * The log of received messages for the replica.
     */
    @JsonIgnore
    private final MessageLog messageLog;

    public FabReplica(
            String nodeId,
            SortedSet<String> nodeIds,
            Transport transport,
            Scenario scenario,
            List<FabRole> roles,
            boolean isCurrentlyLeader,
            long messageTimeoutDuration,
            int numProposers, int numAcceptors, int numLearners, int numFaulty,
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
        this.numProposers = numProposers;
        this.numAcceptors = numAcceptors;
        this.numLearners = numLearners;
        this.numFaulty = numFaulty;
        this.acceptorNodeIds = acceptorNodeIds;
        this.learnerNodeIds = learnerNodeIds;
        this.proposerNodeIds = proposerNodeIds;
        this.leaderId = leaderId;
        this.viewNumber = 1;
        this.messageLog = new MessageLog(this);
    }

    @Override
    public void initialize() {
        log.info("Initializing replica " + getId());
        onStart();
        log.info("Replica " + getId() + " initialized");
    }

    public void onStart() {
        log.info("Replica " + getId() + " is starting");

//        CompletableFuture<Void> leaderTask = isLeader() ? leaderOnStart() : CompletableFuture.completedFuture(null);
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
     * The LEADER starts by sending a PROPOSE message to all ACCEPTOR nodes. It keeps sending the message until
     * the threshold is reached or the time runs out.
     */
    private CompletableFuture<Void> leaderOnStart() {
        log.info("Replica " + getId() + " is the leader and preparing to send a QUERY message to all ACCEPTOR nodes");

        // If the progress certificate is null, the leader is not in the recovery phase and can suggest any value
        if (pc == null) {
            proposedValue = new byte[32];
            new SecureRandom().nextBytes(proposedValue);
        }

        log.info("Replica " + getId() + " is the leader and preparing to send a PROPOSE message to all ACCEPTOR nodes");

        // Resend this message until (p (proposer replicas) + f + 1 ) / 2 <= satisfied.size()
        int threshold = (int) Math.ceil((numProposers + numFaulty + 1) / 2.0);
        Pair proposal = new Pair(viewNumber, proposedValue);
        SortedSet<String> acceptorReceipts = new TreeSet<>(List.of("A", "B", "C", "D", "E", "F"));
        ProposeMessage proposeMessage = new ProposeMessage(this.getId(), proposal, this.pc);

        return CompletableFuture.runAsync(() -> {
            while (!messageLog.isSatisfied(threshold)) {
                multicastMessage(proposeMessage, acceptorReceipts);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.severe("Thread was interrupted during retransmission: " + e.getMessage());
                }
            }

            log.info("The threshold for the number of satisfied messages was reached");
//            this.sendReplyToClient(clientId, proposedValue);
        }, executor);
    }

    /**
     * The PROPOSER starts by waiting to learn the accepted value from the ACCEPTOR nodes.
     * If the PROPOSER does not receive enough LEARN messages, it suspects the leader due to the lack of progress.
     */
    private CompletableFuture<Void> proposerOnStart() {
        int learnedThreshold = (int) Math.ceil((numLearners + numFaulty + 1) / 2.0);

        long endTime = System.currentTimeMillis() + messageTimeoutDuration;
        return CompletableFuture.runAsync(() -> {
            while (messageLog.proposersWithLearnedValueCount() < learnedThreshold && System.currentTimeMillis() < endTime) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.severe("Thread was interrupted: " + e.getMessage());
                }
            }

            if (!messageLog.proposerHasLearned(learnedThreshold)) {
                log.warning("Leader suspected by proposer " + getId());
                broadcastMessage(new SuspectMessage(getId(), this.leaderId, this.viewNumber));
            }
        }, executor);
    }

    /**
     * The LEARNER, if it has not learned a value, sends a PULL message to all LEARNER nodes.
     */
    private CompletableFuture<Void> learnerOnStart() {
        return CompletableFuture.runAsync(() -> {
            while (messageLog.getLearnedValue() == null) {
                try {
                    Thread.sleep(5000);
                    log.info("Learner " + getId() + " sending PULL to all learners...");
                    multicastMessage(new PullMessage(this.viewNumber), this.learnerNodeIds);
                } catch (InterruptedException e) {
                    log.severe("Thread was interrupted: " + e.getMessage());
                }
            }
        }, executor);
    }

    public void electNewLeader() {
        log.info("Electing a new leader...");
        messageLog.deletePreviousRoundMessages(viewNumber);

        // Select the new leader based on the view number and node IDs
        String newLeader = getNewLeader();
        this.viewNumber++;  // Increment the view number to signify a new round
        this.leaderId = newLeader;  // Set the new leader

        isCurrentlyLeader.set(newLeader.equals(getId()));  // This replica is the new leader

        executor.shutdownNow();
        // Notify the other replicas of the new leader
        multicastMessage(new ViewChangeMessage(getId(), viewNumber, newLeader), this.getNodeIds());

        log.info("New leader elected: " + newLeader);
        onElected(viewNumber);
    }

    private String getNewLeader() {
        log.info("Initiating leader election...");
        // Get random node ID from the list of node IDs
        List<String> nodeIdsList = new ArrayList<>(nodeIds);
        Collections.shuffle(nodeIdsList);
        return nodeIdsList.getFirst();
    }

    public void onElected(long newViewNumber) {
        executor.shutdownNow();
        // Update view number to the maximum of the current and new numbers
        viewNumber = Math.max(viewNumber, newViewNumber);

        // If this replica is not the leader for the current number, return
        if (!isLeader()) return;

        // Send QUERY to all acceptors
        log.info("Leader " + getId() + " sending QUERY to all acceptors...");
        multicastMessage(new QueryMessage(viewNumber, pc), acceptorNodeIds);

        // Start the protocol again
        onStart();
    }

    @Override
    public void handleClientRequest(String clientId, Serializable request) throws UnsupportedOperationException {
        this.clientId = clientId;
//        this.proposedValue = (byte[]) request;
        CompletableFuture<Void> leaderTask = isLeader() ? leaderOnStart() : CompletableFuture.completedFuture(null);
        CompletableFuture.allOf(leaderTask).thenRun(() -> {
                    log.info("Replica " + getId() + " finished handling client request.");
                    this.sendReplyToClient(clientId, proposedValue);
                }
        );
    }

    @Override
    public void handleMessage(String sender, MessagePayload message) throws UnsupportedOperationException {
        log.info(String.format("Replica %s received a message from %s: %s", getId(), sender, message));

        messageLog.addMessage(message);

        if (message instanceof ProposeMessage && isAcceptor()) {
            handleProposeMessage(sender, (ProposeMessage) message);
        } else if (message instanceof AcceptMessage && isLearner()) {
            handleAcceptMessage(sender, (AcceptMessage) message);
        } else if (message instanceof SatisfiedMessage && isProposer()) {
            handleSatisfiedMessage(sender, (SatisfiedMessage) message);
        } else if (message instanceof LearnMessage) {
            if (isProposer()) handleLearnMessageProposer(sender, (LearnMessage) message);
            if (isLearner()) handleLearnMessageLearner(sender, (LearnMessage) message);
        } else if (message instanceof PullMessage && isLearner()) {
            handlePullMessage(sender, (PullMessage) message);
        } else if (message instanceof QueryMessage && isAcceptor()) {
            handleQueryMessage(sender, (QueryMessage) message);
        } else if (message instanceof SuspectMessage) {
            handleSuspectMessage(sender, (SuspectMessage) message);
        } else if (message instanceof ReplyMessage) {
            handleReplyMessage(sender, (ReplyMessage) message);
        } else if (message instanceof ViewChangeMessage) {
            handleViewChangeMessage(sender, (ViewChangeMessage) message);
        } else if (message instanceof DefaultClientRequestPayload clientRequest) {
            handleClientRequest(sender, clientRequest.getOperation());
        }

        else {
            throw new UnsupportedOperationException("Unknown message type: " + message.getType());
        }
    }

    /**
     * Handle a PROPOSE message send by a replica with Proposer role, who is the leader, received by all Acceptor replicas.
     * @param sender : the nodeId of the sender (the current leader)
     * @param proposeMessage : the PROPOSE message with the proposed value and round number
     */
    private void handleProposeMessage(String sender, ProposeMessage proposeMessage) {

        int vouchingThreshold = (int) Math.ceil((numAcceptors - numFaulty + 1) / 2.0);

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
        int acceptedThreshold = (int) Math.ceil((numAcceptors + (3 * numFaulty) + 1) / 2.0);
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
        int learnedThreshold = (int) Math.ceil((numLearners + numFaulty + 1) / 2.0);

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
        int learningThreshold = numFaulty + 1;
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
        int quorum = numAcceptors - numFaulty;
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
        int quorum = numFaulty + 1;

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
//        long newViewNumber = viewChangeMessage.getNewViewNumber();
//        String newLeaderId = viewChangeMessage.getNewLeaderId();
//
//        // If the new view number is greater than the current view number, update the view number and leader ID
//        if (newViewNumber > viewNumber) {
//            viewNumber = newViewNumber;
//            leaderId = newLeaderId;
//        }

        executor.shutdownNow();
        messageLog.acceptViewChange();
        initialize();
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
