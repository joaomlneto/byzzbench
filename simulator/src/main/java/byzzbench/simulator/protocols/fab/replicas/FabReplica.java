package byzzbench.simulator.protocols.fab.replicas;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.protocols.fab.ProgressCertificate;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A replica in the FAB protocol.
 */
@Log
@Getter
public class FabReplica extends LeaderBasedProtocolReplica {
    List<FabRole> roles;

    // The number of replicas with each role
    private final int proposersNumber;
    private final int acceptorsNumber;
    private final int learnersNumber;
    private final int byzantinesNumber;

    // Acceptor role
    private Pair acceptedValue;

    // Learner role
    Pair[] accepted;
    Pair[] learn;
    Pair learned;

    // Proposer role
    private final Map<String, Pair> satisfied;
    private final Set<String> learnedNodes;
    private long viewNumber;
    private byte[] proposedValue;
    private ProgressCertificate pc;

    private boolean isLeader;

    private final long timeout;

    private long lastLeaderMessageTime = 0L;

    private String leaderId;

    int queried;

    List<String> nodesSuspectingLeader;

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
            boolean isLeader,
            long timeout,
            int proposersNumber, int acceptorsNumber, int learnersNumber, int byzantinesNumber) {
        super(nodeId, nodeIds, transport, new TotalOrderCommitLog());
        this.messageLog = messageLog;
        this.roles = roles;
        this.timeout = timeout;
        this.satisfied = new HashMap<>();
        this.learnedNodes = new HashSet<>();
        this.isLeader = isLeader;
        this.proposersNumber = proposersNumber;
        this.acceptorsNumber = acceptorsNumber;
        this.learnersNumber = learnersNumber;
        this.byzantinesNumber = byzantinesNumber;
        this.nodesSuspectingLeader = new ArrayList<>();
    }

    @Override
    public void initialize() {
        log.info("Initializing replica " + getNodeId());
        this.setView(1);

        // Initialize the replica based on the roles it has
        if (isAcceptor()) {
            log.info("Replica " + getNodeId() + " is an acceptor");
            acceptedValue = null;
        }

        if (isLearner()) {
            log.info("Replica " + getNodeId() + " is a learner");
            accepted = new Pair[getNodeIds().size()];
            learn = new Pair[getNodeIds().size()];
            learned = null;
        }

        if (isProposer()) {
            log.info("Replica " + getNodeId() + " is a proposer");
            viewNumber = 0;
            proposedValue = null;
            pc = null;
        }

        log.info("Replica " + getNodeId() + " initialized");
        log.info("onStart method initialized");
        onStart();
        log.info("onStart method finished");
    }

    public void onStart() {
        log.info("Replica " + getNodeId() + " is starting");

        if (isLeader()) leaderOnStart();
        if (isProposer()) proposerOnStart();
        if (isLearner()) learnerOnStart();

        log.info("Replica " + getNodeId() + " is ready");
    }

    private void leaderOnStart() {
        // Query the Acceptor replicas for their progress certificates
        log.info("Replica " + getNodeId() + " is the leader and preparing to send a QUERY message to all ACCEPTOR nodes");

        this.viewNumber++;

        // If the progress certificate is null, the leader is not in the recovery phase and can suggest any value
        if (pc == null) {
            SecureRandom random = new SecureRandom();
            byte[] value = new byte[32];
            random.nextBytes(value);
            proposedValue = value;
        }

        // If the replica is a leader, send a PROPOSE message to all ACCEPTOR nodes
        log.info("Replica " + getNodeId() + " is the leader and preparing to send a PROPOSE message to all ACCEPTOR nodes");

        // Resend this message until (p (proposer replicas) + f + 1 ) / 2 <= satisfied.size()
        // Count the number of proposer replicas

        // Calculate the threshold for the number of satisfied messages needed
        int threshold = (int) Math.floor((proposersNumber + byzantinesNumber + 1) / 2.0);

        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeout;

        log.info("Sending PROPOSE message to " + proposersNumber + " nodes...");
        Pair proposal = new Pair(viewNumber, proposedValue);
        while (System.currentTimeMillis() < endTime && satisfied.size() < threshold) {
            getNodeIds().stream()
                    .filter(nodeId -> roles.contains(FabRole.ACCEPTOR))
                    .forEach(nodeId -> getTransport().
                            sendMessage(getNodeId(),
                                    new ProposeMessage(
                                            this.getNodeId(),
                                            proposal,
                                            this.pc),
                                    nodeId));

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.severe("Thread was interrupted during retransmission: " + e.getMessage());
            }
        }

        if (satisfied.size() < threshold) {
            log.warning(String.format("The threshold for the number of satisfied messages was not reached, node %s is suspected", getNodeId()));

        } else {
            log.info("The threshold for the number of satisfied messages was reached");
            this.electNewLeader();
        }
    }

    private void proposerOnStart() {
        int learnedThreshold = (int) Math.floor((learnersNumber + byzantinesNumber + 1) / 2.0);
        // If the replica is a proposer, wait for a timeout
        long timeStarted = System.currentTimeMillis();
        while (learn.length < learnedThreshold && System.currentTimeMillis() < timeStarted + timeout) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.severe("Thread was interrupted: " + e.getMessage());
            }
        }

        if (learn.length < learnedThreshold) {
            // Suspect the leader.
            log.warning("Leader suspected");
            nodesSuspectingLeader.add(leaderId);
            String leader = computePrimaryId(viewNumber, getNodeIds().size());

            getTransport().multicast(
                    getNodeId(),
                    getNodeIds(),
                    new SuspectMessage(this.getNodeId(), leader)
            );
        }
    }

    private void learnerOnStart() {
        // If the replica is a learner, wait for a timeout
        while (learned == null) {
            try {
                Thread.sleep(1000);
                // Sent PULL message to all LEARNER nodes
                log.info("Learner " + getNodeId() + " sending PULL to all learner...");
                for (String nodeId : getNodeIds()) {
                    if (roles.contains(FabRole.LEARNER)) {
                        getTransport().sendMessage(
                                getNodeId(),
                                new PullMessage(),
                                nodeId
                        );
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.severe("Thread was interrupted: " + e.getMessage());
            }
        }
    }

    public void electNewLeader() {
        System.out.println("Electing a new leader...");
        // Implement leader election logic here
        String newLeader = getNewLeader();
        this.setView(viewNumber);
    }

    private String getNewLeader() {
        log.info("Initiating leader election...");
        List<String> knownReplicas = this.getNodeIds().stream().sorted().toList();
        int numReplicas = getNodeIds().size();
        return knownReplicas.get((int) viewNumber % numReplicas);
    }

    public void setView(long viewNumber) {
        this.viewNumber = viewNumber;
        this.leaderId = computePrimaryId(viewNumber, getNodeIds().size());
        this.setView(viewNumber, leaderId);
    }

    private String computePrimaryId(long viewNumber, int numReplicas) {
        List<String> knownReplicas = this.getNodeIds().stream().sorted().toList();
        return knownReplicas.get((int) viewNumber % numReplicas);
    }

    public boolean validateProgressCertificate() {
        // Implement progress certificate validation logic here
        return true;
    }

    public void onElected(int newNumber) {
        // Check if this replica is the leader for the current number
        if (!isLeader || viewNumber != newNumber) return;

        this.viewNumber = Math.max(viewNumber, newNumber);

        Pair proposal = new Pair(this.viewNumber, this.proposedValue);
        // Send QUERY message to all ACCEPTOR nodes
        while (System.currentTimeMillis() < lastLeaderMessageTime + timeout) {
            getNodeIds().stream()
                    .filter(nodeId -> roles.contains(FabRole.ACCEPTOR))
                    .forEach(nodeId -> getTransport().
                            sendMessage(getNodeId(),
                                    new QueryMessage(proposal, pc),
                                    nodeId));

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.severe("Thread was interrupted during retransmission: " + e.getMessage());
            }
        }

        // Send QUERY to all acceptors
        log.info("Leader " + getNodeId() + " sending QUERY to all acceptors...");
        for (String nodeId : getNodeIds()) {
            if (roles.contains(FabRole.ACCEPTOR)) {
                getTransport().sendMessage(
                        getNodeId(),
                        new QueryMessage(proposal, pc),
                        nodeId
                );
            }
        }

        // Wait for responses from acceptors
//        List<ProgressCertificate> responses = collectResponsesFromAcceptors();
//        ProgressCertificate newPc = mergeProgressCertificates(responses);

        // Update value if the new progress certificate vouches for it
//        if (newPc != null && newPc.vouchesFor(proposedValue)) {
//            proposedValue = newPc.getValue();
//        }

        onStart(); // Restart protocol with the new leader
    }

    public void onQuery(int proposalNumber, ProgressCertificate proof) {
        // Ignore invalid requests
        if (proof == null || proposalNumber < this.viewNumber) return;

        // Update largest proposal number
        this.viewNumber = proposalNumber;

        // Send reply to the leader
        getTransport().sendMessage(
                getNodeId(),
                new ResponseMessage(new Pair(this.viewNumber, this.proposedValue)),
                getLeaderId()
        );
    }

    @Override
    public void handleClientRequest(String clientId, Serializable request) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Client requests not supported in Fast Byzantine Consensus");
    }

    @Override
    public void handleMessage(String sender, MessagePayload message) throws UnsupportedOperationException {
        log.info(String.format("Replica %s received a message from %s: %s", getNodeId(), sender, message));
        // Possible messages: ProposeMessage, AcceptMessage, LearnMessage, SatisfiedMessage, QueryMessage, ResponseMessage
        if (message instanceof ProposeMessage) {
            // Acceptors must process PROPOSE messages
            if (isAcceptor()) {
                handleProposeMessage(sender, (ProposeMessage) message);
                this.lastLeaderMessageTime = System.currentTimeMillis();
            }
        } else if (message instanceof AcceptMessage) {
            // Learner replicas must process ACCEPT messages
            if (isLearner()) {
                handleAcceptMessage(sender, (AcceptMessage) message);
            }
        } else if (message instanceof SatisfiedMessage) {
            // Proposers must process SATISFIED messages
            if (isProposer()) {
                handleSatisfiedMessage(sender, (SatisfiedMessage) message);
            }
        } else if (message instanceof LearnMessage) {
            // Learner replicas must process LEARN messages
            if (isProposer()) {
                handleLearnMessageProposer(sender, (LearnMessage) message);
            }
            if (isLearner()) {
                handleLearnMessageLearner(sender, (LearnMessage) message);
            }
        } else if (message instanceof PullMessage) {
            // Learner replicas must process PULL messages
            if (isLearner()) {
                handlePullMessage(sender, (PullMessage) message);
            }
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
        // If the PROPOSE message has a higher round number than the current round number, update the round number
        long messageViewNumber = proposeMessage.getValueAndProposalNumber().getNumber();
        byte[] messageProposedValue = proposeMessage.getValueAndProposalNumber().getValue();
        ProgressCertificate progressCertificate = proposeMessage.getProgressCertificate();

        if (messageViewNumber != this.viewNumber) return; // Only listen to current leader

        if (acceptedValue != null && acceptedValue.getNumber() == messageViewNumber) return; // Ignore duplicate proposals

        if (acceptedValue != null && !Arrays.equals(acceptedValue.getValue(), messageProposedValue) &&
                !progressCertificate.vouchesFor(messageProposedValue)) {
            return; // Change only allowed with a valid progress certificate
        }

        // Accept the proposal
        acceptedValue = new Pair(messageViewNumber, messageProposedValue);

        // Notify learners
        for (String nodeId : getNodeIds()) {
            if (roles.contains(FabRole.LEARNER)) {
                getTransport().sendMessage(
                        getNodeId(),
                        new AcceptMessage(this.getNodeId(), acceptedValue),
                        nodeId
                );
            }
        }
    }

    /**
     * Handle an ACCEPT message sent by an Acceptor replica, received by a Learner replica.
     * @param sender : the nodeId of the sender (an Acceptor replica)
     * @param acceptMessage : the ACCEPT message with the value and proposal number
     */
    private void handleAcceptMessage(String sender, AcceptMessage acceptMessage) {
        Pair acceptValue = acceptMessage.getValueAndProposalNumber();
        accepted[sender.charAt(0) - 'A'] = acceptValue;

        log.info("Acceptor " + getNodeId() + " received ACCEPT from " + sender + " and proposal number " + acceptValue.getNumber());

        byte[] acceptedValue = acceptMessage.getValueAndProposalNumber().getValue();
        long acceptedNumber = acceptMessage.getValueAndProposalNumber().getNumber();
        int acceptedThreshold = (int) Math.floor((acceptorsNumber + (3 * byzantinesNumber) + 1) / 2.0);
        AtomicInteger currentAccepted = new AtomicInteger();
        // If there are acceptedThreshold accepted values for the same proposalValue, send a LEARN message to all Proposer replicas
        Arrays.stream(accepted).filter(Objects::nonNull).forEach(pair -> {
            if (pair.getNumber() == acceptedNumber && Arrays.equals(pair.getValue(), acceptedValue)) {
                currentAccepted.getAndIncrement();
            }
        });

        log.info("The number of accepted values for the same proposal value is " + currentAccepted.get());

        if (currentAccepted.get() >= acceptedThreshold) {
            // Send LEARN message to all LEARNER nodes
            log.info("Acceptor " + getNodeId() + " sending LEARN to all proposer...");
            for (String nodeId : getNodeIds()) {
                if (roles.contains(FabRole.LEARNER)) {
                    getTransport().sendMessage(
                            getNodeId(),
                            new LearnMessage(acceptValue),
                            nodeId
                    );
                }
            }
        }
    }

    private void handleLearnMessageProposer(String sender, LearnMessage learnMessage) {
        Pair learnValue = learnMessage.getValueAndProposalNumber();
        learn[sender.charAt(0) - 'A'] = learnValue;

        int learnedThreshold = (int) Math.floor((learnersNumber + byzantinesNumber + 1) / 2.0);
        if (learn.length >= learnedThreshold) {
            // Send SATISFIED message to all PROPOSER nodes
            log.info("Proposer " + getNodeId() + " sending SATISFIED to all proposer...");
            for (String nodeId : getNodeIds()) {
                if (roles.contains(FabRole.PROPOSER)) {
                    getTransport().sendMessage(
                            getNodeId(),
                            new SatisfiedMessage(this.getNodeId(), learn[0]),
                            nodeId
                    );
                }
            }
        }

    }

    private void handleLearnMessageLearner(String sender, LearnMessage learnMessage) {
        Pair learnValue = learnMessage.getValueAndProposalNumber();
        learn[sender.charAt(0) - 'A'] = learnValue;

        AtomicInteger currentLearnedWithSamePair = new AtomicInteger();
        Arrays.stream(learn).filter(Objects::nonNull).forEach(pair -> {
            if (pair.equals(learnValue)) {
                currentLearnedWithSamePair.getAndIncrement();
            }
        });

        int learningThreshold = byzantinesNumber + 1;
        if (currentLearnedWithSamePair.get() >= learningThreshold) {
            learned = learnValue;
        }
    }

    /**
     * Handle a SATISFIED message received by a Proposer replica.
     * @param sender : the nodeId of the sender (a Proposer replica)
     * @param satisfiedMessage : the SATISFIED message with the value and proposal number
     */
    private void handleSatisfiedMessage(String sender, SatisfiedMessage satisfiedMessage) {
        satisfied.put(sender, satisfiedMessage.getValueAndProposalNumber());
    }

    private void handleQueryMessage(String sender, QueryMessage queryMessage) {
        long messageViewNumber = queryMessage.getValueAndProposalNumber().getNumber();
        ProgressCertificate proof = queryMessage.getProgressCertificate();

        if (proof == null || proof.isValid(acceptorsNumber - byzantinesNumber) || messageViewNumber < this.viewNumber) return;

        this.viewNumber = messageViewNumber;

    }

    private void handlePullMessage(String sender, PullMessage pullMessage) {
        // If this learner has learned a value, send it to the sender
        if (learned != null) {
            getTransport().sendMessage(
                    getNodeId(),
                    new LearnMessage(learned),
                    sender
            );
        }
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
}
