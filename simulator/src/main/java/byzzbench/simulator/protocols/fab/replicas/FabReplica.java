package byzzbench.simulator.protocols.fab.replicas;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.protocols.fab.ProgressCertificate;
import byzzbench.simulator.protocols.fab.SignedResponse;
import byzzbench.simulator.protocols.fab.messages.*;
import byzzbench.simulator.protocols.pbft_java.MessageLog;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

    // Acceptor role
    private Pair currentAcceptedProposal;

    // Learner role
    SortedMap<String, Pair> acceptorsWithAcceptedProposal;
    SortedMap<String, Pair> proposersWithLearnedValue;
    Pair learnedValue;
    List<SignedResponse> responses;

    // Proposer role
    private Map<String, Pair> satisfiedProposerNodes;
    private SortedMap<String, Pair> learnersWithLearnedValue;
    private long viewNumber;
    private byte[] proposedValue;
    private ProgressCertificate pc;

    private final AtomicBoolean isCurrentlyLeader = new AtomicBoolean(false);

    private final long messageTimeoutDuration;

    private long lastLeaderMessageTime = 0L;

    private String leaderId;

    List<String> nodesSuspectingLeader;

    SortedSet<String> acceptorNodeIds;
    SortedSet<String> learnerNodeIds;
    SortedSet<String> proposerNodeIds;

    /**
     * The log of received messages for the replica.
     */
    @JsonIgnore
    private final MessageLog messageLog;

    public FabReplica(
            String nodeId,
            SortedSet<String> nodeIds,
            Transport transport,
            MessageLog messageLog,
            List<FabRole> roles,
            boolean isCurrentlyLeader,
            long messageTimeoutDuration,
            int numProposers, int numAcceptors, int numLearners, int numFaulty,
            SortedSet<String> acceptorNodeIds,
            SortedSet<String> learnerNodeIds,
            SortedSet<String> proposerNodeIds,
            String leaderId) {
        super(nodeId, nodeIds, transport, new TotalOrderCommitLog());
        this.messageLog = messageLog;
        this.roles = roles;
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
    }

    @Override
    public void initialize() {
        log.info("Initializing replica " + getNodeId());

        initializeState();

        log.info("Replica " + getNodeId() + " initialized");
        onStart();
    }

    private void initializeState() {
        viewNumber = 1;
        this.acceptorsWithAcceptedProposal = new TreeMap<>();
        this.proposersWithLearnedValue = new TreeMap<>();
        this.satisfiedProposerNodes = new TreeMap<>();
        this.learnersWithLearnedValue = new TreeMap<>();
        this.nodesSuspectingLeader = new ArrayList<>();
        this.responses = new ArrayList<>();
    }

    public void onStart() {
        log.info("Replica " + getNodeId() + " is starting");

        CompletableFuture<Void> leaderTask = isLeader() ? leaderOnStart() : CompletableFuture.completedFuture(null);
        CompletableFuture<Void> proposerTask = isProposer() ? proposerOnStart() : CompletableFuture.completedFuture(null);
        CompletableFuture<Void> learnerTask = isLearner() ? learnerOnStart() : CompletableFuture.completedFuture(null);

        CompletableFuture.allOf(leaderTask, proposerTask, learnerTask).thenRun(() ->
                log.info("Replica " + getNodeId() + " finished starting.")
        );
    }

    /**
     * The LEADER starts by sending a PROPOSE message to all ACCEPTOR nodes. It keeps sending the message until
     * the threshold is reached or the time runs out.
     */
    private CompletableFuture<Void> leaderOnStart() {
        return CompletableFuture.runAsync(() -> {
            log.info("Replica " + getNodeId() + " is the leader and preparing to send a QUERY message to all ACCEPTOR nodes");

            // If the progress certificate is null, the leader is not in the recovery phase and can suggest any value
            if (pc == null) {
                proposedValue = new byte[32];
                new SecureRandom().nextBytes(proposedValue);
            }

            log.info("Replica " + getNodeId() + " is the leader and preparing to send a PROPOSE message to all ACCEPTOR nodes");

            // Resend this message until (p (proposer replicas) + f + 1 ) / 2 <= satisfied.size()
            int threshold = (int) Math.ceil((numProposers + numFaulty + 1) / 2.0);
            long endTime = System.currentTimeMillis() + messageTimeoutDuration;
            Pair proposal = new Pair(viewNumber, proposedValue);

            sendProposeMessage(endTime, proposal, threshold);
            isSatisfied(threshold);
        }, Executors.newCachedThreadPool());
    }

    private void sendProposeMessage(long endTime, Pair proposal, int threshold) {
        SortedSet<String> acceptorsReceipts = new TreeSet<>(List.of("A", "B", "C", "D", "E", "F"));
        CompletableFuture.runAsync(() -> {
            log.info("Sending PROPOSE message asynchronously to " + numProposers + " nodes...");
            while (System.currentTimeMillis() < endTime && satisfiedProposerNodes.size() < threshold) {
                multicastMessage(new ProposeMessage(this.getNodeId(), proposal, this.pc), acceptorsReceipts);
                try {
                    Thread.sleep(40000); // Note: Still blocking within async task
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.severe("Thread was interrupted during retransmission: " + e.getMessage());
                }
            }
        }, Executors.newCachedThreadPool());
    }

    private void isSatisfied(int threshold) {
        if (satisfiedProposerNodes.size() < threshold) {
            log.warning(String.format("The threshold for the number of satisfied messages was not reached, node %s is suspected", getNodeId()));
        } else {
            log.info("The threshold for the number of satisfied messages was reached");
        }
    }

    /**
     * The PROPOSER starts by waiting to learn the accepted value from the ACCEPTOR nodes.
     * If the PROPOSER does not receive enough LEARN messages, it suspects the leader due to the lack of progress.
     */
    private CompletableFuture<Void> proposerOnStart() {
        return CompletableFuture.runAsync(() -> {
            int learnedThreshold = (int) Math.ceil((numLearners + numFaulty + 1) / 2.0);
            long endTime = System.currentTimeMillis() + messageTimeoutDuration;

            while (proposersWithLearnedValue.size() < learnedThreshold && System.currentTimeMillis() < endTime) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.severe("Thread was interrupted: " + e.getMessage());
                }
            }

            if (proposersWithLearnedValue.size() < learnedThreshold) {
                log.warning("Leader suspected by proposer " + getNodeId());
                nodesSuspectingLeader.add(getNodeId());
                broadcastMessage(new SuspectMessage(getNodeId(), this.leaderId));
            }
        }, Executors.newCachedThreadPool());
    }

    /**
     * The LEARNER, if it has not learned a value, sends a PULL message to all LEARNER nodes.
     */
    private CompletableFuture<Void> learnerOnStart() {
        return CompletableFuture.runAsync(() -> {
            while (this.learnedValue == null) {
                try {
                    Thread.sleep(40000);
                    log.info("Learner " + getNodeId() + " sending PULL to all learners...");
                    multicastMessage(new PullMessage(), this.learnerNodeIds);
                } catch (InterruptedException e) {
                    log.severe("Thread was interrupted: " + e.getMessage());
                }
            }
        }, Executors.newCachedThreadPool());
    }

    public void electNewLeader() {
        log.info("Electing a new leader...");

        // Select the new leader based on the view number and node IDs
        String newLeader = getNewLeader();
        this.viewNumber++;  // Increment the view number to signify a new round
        this.leaderId = newLeader;  // Set the new leader

        // Mark this replica as the leader if it matches the elected leader
        if (newLeader.equals(getNodeId())) {
            isCurrentlyLeader.set(true);  // This replica is the new leader
            onStart();  // Start the protocol again with the new leader
        } else {
            isCurrentlyLeader.set(false);  // This replica is not the leader
        }

        // Notify the other replicas of the new leader
        multicastMessage(new ViewChangeMessage(getNodeId(), viewNumber, newLeader), this.getNodeIds());

        log.info("New leader elected: " + newLeader);
        onElected((int) viewNumber);
    }

    private String getNewLeader() {
        log.info("Initiating leader election...");
        List<String> knownReplicas = this.getNodeIds().stream().sorted().toList();
        int numReplicas = getNodeIds().size();
        return knownReplicas.get((int) viewNumber % numReplicas);
    }

    public void onElected(int newNumber) {
        // Update view number to the maximum of the current and new numbers
        viewNumber = Math.max(viewNumber, newNumber);

        // If this replica is not the leader for the current number, return
        if (!isLeader()) return;

        // Send QUERY to all acceptors
        long endTime = System.currentTimeMillis() + messageTimeoutDuration + 10000L;
        log.info("Leader " + getNodeId() + " sending QUERY to all acceptors...");
        while (System.currentTimeMillis() < endTime && responses.size() < numAcceptors) {
            multicastMessage(new QueryMessage(viewNumber, pc), acceptorNodeIds);
        }

        // Combine the received responses into a progress certificate (PC)
        ProgressCertificate progressCertificate = new ProgressCertificate(this.viewNumber, responses);

        // If PC vouches for the value, update the value
        int vouchingThreshold = (int) Math.ceil((numAcceptors - numFaulty + 1) / 2.0);
        Optional<byte[]> vouchedValue = progressCertificate.majorityValue(vouchingThreshold);
        if (vouchedValue.isPresent()) {
            proposedValue = vouchedValue.get();
            pc = progressCertificate;
        } else {
            log.warning("Progress certificate does not vouch for the value");
        }

        // Restart protocol with the new leader
        onStart();
    }

    @Override
    public void handleClientRequest(String clientId, Serializable request) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Client requests not supported in Fast Byzantine Consensus");
    }

    @Override
    public void handleMessage(String sender, MessagePayload message) throws UnsupportedOperationException {
        log.info(String.format("Replica %s received a message from %s: %s", getNodeId(), sender, message));

        if (message instanceof ProposeMessage && isAcceptor()) {
            handleProposeMessage(sender, (ProposeMessage) message);
            this.lastLeaderMessageTime = System.currentTimeMillis();
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
        }

        else {
            throw new UnsupportedOperationException("Unknown message type: " + message.getType());
        }
    }

    private void handleSuspectMessage(String sender, SuspectMessage suspectMessage) {
        log.info("Replica " + getNodeId() + " received SUSPECT from " + sender);
        nodesSuspectingLeader.add(sender);
        if (nodesSuspectingLeader.size() >= numFaulty + 1) {
            electNewLeader();
        }
    }

    public void handleReplyMessage(String sender, ReplyMessage replyMessage) {
        byte[] value = replyMessage.getValueAndProposalNumber().getValue();
        long viewNumber = replyMessage.getValueAndProposalNumber().getNumber();
        boolean isSigned = replyMessage.isSigned();
        String replySender = replyMessage.getSender();
        responses.add(new SignedResponse(value, viewNumber, isSigned, replySender));
    }

    /**
     * Handle a PROPOSE message send by a replica with Proposer role, who is the leader, received by all Acceptor replicas.
     * @param sender : the nodeId of the sender (the current leader)
     * @param proposeMessage : the PROPOSE message with the proposed value and round number
     */
    private void handleProposeMessage(String sender, ProposeMessage proposeMessage) {
        // If the PROPOSE message has a higher round number than the current round number, update the round number
        long messageViewNumber = proposeMessage.getValueAndProposalNumber().getNumber();
        byte[] messageProposedValue = proposeMessage.getValueAndProposalNumber().getValue();
        ProgressCertificate progressCertificate = proposeMessage.getProgressCertificate();

        // Only listen to current leader
        if (messageViewNumber != this.viewNumber) {
            log.info("Acceptor " + getNodeId() + " ignoring PROPOSE message with round number " + messageViewNumber);
            return;
        }

        // Ignore duplicate proposals
        if (currentAcceptedProposal != null && currentAcceptedProposal.getNumber() == messageViewNumber) {
            log.info("Acceptor " + getNodeId() + " already accepted a proposal with round number " + messageViewNumber);
            return;
        }

        int vouchingThreshold = (int) Math.ceil((numAcceptors - numFaulty + 1) / 2.0);
        if (currentAcceptedProposal != null && !Arrays.equals(currentAcceptedProposal.getValue(), messageProposedValue) &&
                !progressCertificate.vouchesFor(messageProposedValue, vouchingThreshold)) {
            log.info("Acceptor " + getNodeId() + " ignoring PROPOSE message with round number " + messageViewNumber);
            return;
        }

        // Accept the proposal
        currentAcceptedProposal = new Pair(messageViewNumber, messageProposedValue);
        log.info("Acceptor " + getNodeId() + " accepted proposal with value " + new String(messageProposedValue));

        multicastMessage(new AcceptMessage(getNodeId(), currentAcceptedProposal), this.learnerNodeIds);
    }

    /**
     * Handle an ACCEPT message sent by an Acceptor replica, received by a Learner replica.
     * @param sender : the nodeId of the sender (an Acceptor replica)
     * @param acceptMessage : the ACCEPT message with the value and proposal number
     */
    private void handleAcceptMessage(String sender, AcceptMessage acceptMessage) {
        log.info("Learner " + getNodeId() + " received ACCEPT from " + sender + " and proposal number " + acceptMessage.getValueAndProposalNumber().getNumber());
        Pair acceptValue = acceptMessage.getValueAndProposalNumber();
        acceptorsWithAcceptedProposal.put(sender, acceptValue);

        log.info("Acceptor " + getNodeId() + " received ACCEPT from " + sender + " and proposal number " + acceptValue.getNumber());

        byte[] acceptedValue = acceptMessage.getValueAndProposalNumber().getValue();
        long acceptedNumber = acceptMessage.getValueAndProposalNumber().getNumber();
        int acceptedThreshold = (int) Math.ceil((numAcceptors + (3 * numFaulty) + 1) / 2.0);
        AtomicInteger currentAccepted = new AtomicInteger();
        // If there are acceptedThreshold accepted values for the same proposalValue, send a LEARN message to all Proposer replicas
        acceptorsWithAcceptedProposal.values().forEach(pair -> {
            if (pair.getNumber() == acceptedNumber && Arrays.equals(pair.getValue(), acceptedValue)) {
                currentAccepted.getAndIncrement();
            }
        });

        log.info("The number of accepted values for the same proposal value is " + currentAccepted.get());
        if (currentAccepted.get() >= acceptedThreshold) {
            learnedValue = acceptValue;
            log.info("Acceptor " + getNodeId() + " sending LEARN to all proposer...");
            multicastMessage(new LearnMessage(acceptValue), this.proposerNodeIds);
        }
    }

    private void handleLearnMessageProposer(String sender, LearnMessage learnMessage) {
        log.info("Proposer " + getNodeId() + " received LEARN from " + sender + " and proposal number " + learnMessage.getValueAndProposalNumber().getNumber());
        Pair learnValue = learnMessage.getValueAndProposalNumber();
        proposersWithLearnedValue.put(sender, learnValue);

        int learnedThreshold = (int) Math.ceil((numLearners + numFaulty + 1) / 2.0);
        if (proposersWithLearnedValue.size() >= learnedThreshold) {
            // Send SATISFIED message to all PROPOSER nodes
            log.info("Proposer " + getNodeId() + " sending SATISFIED to all proposer...");
            multicastMessage(new SatisfiedMessage(getNodeId(), learnValue), this.proposerNodeIds);
        }
    }

    private void handleLearnMessageLearner(String sender, LearnMessage learnMessage) {
        log.info("Learner " + getNodeId() + " received LEARN from " + sender + " and proposal number " + learnMessage.getValueAndProposalNumber().getNumber());
        Pair learnValue = learnMessage.getValueAndProposalNumber();
        learnersWithLearnedValue.put(sender, learnValue);

        AtomicInteger currentLearnedWithSamePair = new AtomicInteger();
        proposersWithLearnedValue.values().forEach(pair -> {
            if (pair.getNumber() == learnValue.getNumber() && Arrays.equals(pair.getValue(), learnValue.getValue())) {
                currentLearnedWithSamePair.getAndIncrement();
            }
        });

        int learningThreshold = numFaulty + 1;
        if (currentLearnedWithSamePair.get() >= learningThreshold && learnedValue == null) {
            learnedValue = learnValue;
        }
    }

    /**
     * Handle a SATISFIED message received by a Proposer replica.
     * @param sender : the nodeId of the sender (a Proposer replica)
     * @param satisfiedMessage : the SATISFIED message with the value and proposal number
     */
    private void handleSatisfiedMessage(String sender, SatisfiedMessage satisfiedMessage) {
        log.info("Proposer " + getNodeId() + " received SATISFIED from " + sender + " and proposal number " + satisfiedMessage.getValueAndProposalNumber().getNumber());
        satisfiedProposerNodes.put(sender, satisfiedMessage.getValueAndProposalNumber());
    }

    private void handleQueryMessage(String sender, QueryMessage queryMessage) {
        long messageViewNumber = queryMessage.getViewNumber();
        ProgressCertificate proof = queryMessage.getProgressCertificate();

        // Ignore bad requests.
        if (proof == null || !proof.isValid(numAcceptors - numFaulty) || messageViewNumber < this.viewNumber) {
            return;
        }

        this.viewNumber = messageViewNumber;
        // Send a REPLY message to the leader.
        // Sign the current accepted value and the current view number.
        sendMessage(new ReplyMessage(new Pair(viewNumber, currentAcceptedProposal.getValue()), true, sender), leaderId);
    }

    private void handlePullMessage(String sender, PullMessage pullMessage) {
        log.info("Learner " + getNodeId() + " received PULL from " + sender);
        // If this learner has learned a value, send it to the sender
        if (learnedValue != null) {
            sendMessage(new LearnMessage(learnedValue), sender);
        }
    }

    private void handleViewChangeMessage(String sender, ViewChangeMessage viewChangeMessage) {
        long newViewNumber = viewChangeMessage.getViewNumber();
        String newLeaderId = viewChangeMessage.getLeaderId();

        // If the new view number is greater than the current view number, update the view number and leader ID
        if (newViewNumber > viewNumber) {
            viewNumber = newViewNumber;
            leaderId = newLeaderId;
        }

        onElected((int) viewNumber);
        onStart();
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
