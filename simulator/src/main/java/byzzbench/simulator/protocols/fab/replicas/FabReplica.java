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

/**
 * A replica in the FAB protocol.
 */
@Log
@Getter
public class FabReplica extends LeaderBasedProtocolReplica {
    List<FabRole> roles;

    // Acceptor role
    private Pair acceptedValue;

    // Learner role
    Pair[] accepted;
    Pair[] learn;
    Pair learned;

    // Proposer role
    private final Map<String, Pair> satisfied;
    private final Set<String> learnedNodes;
    private long proposalNumber;
    private byte[] proposedValue;
    private ProgressCertificate pc;

    private boolean isLeader;

    private final long timeout;

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
            boolean isLeader, long timeout) {
        super(nodeId, nodeIds, transport, new TotalOrderCommitLog());
        this.messageLog = messageLog;
        this.roles = roles;
        this.timeout = timeout;
        this.satisfied = new HashMap<>();
        this.learnedNodes = new HashSet<>();
        this.isLeader = isLeader;
    }

    @Override
    public void initialize() {
        log.info("Initializing replica " + getNodeId());
        // Initialize the replica based on the roles it has
        if (isAcceptor()) {
            acceptedValue = null;
        }

        if (isLearner()) {
            accepted = new Pair[getNodeIds().size()];
            learn = new Pair[getNodeIds().size()];
            learned = null;
        }

        if (isProposer()) {
            proposalNumber = 0;
            proposedValue = null;
            pc = null;
        }

        log.info("Replica " + getNodeId() + " initialized");
        log.info("onStart method initialized");
        onStart();
        log.info("onStart method finished");
    }

    public void onStart() {
        if (isLeader()) leaderOnStart();
        if (isProposer()) proposerOnStart();
        if (isLearner()) learnerOnStart();
    }

    private void leaderOnStart() {
        // Query the Acceptor replicas for their progress certificates
//        log.info("Replica " + getNodeId() + " is the leader and preparing to send a QUERY message to all ACCEPTOR nodes");

        // If the replica is a leader, send a PROPOSE message to all ACCEPTOR nodes
        log.info("Replica " + getNodeId() + " is the leader and preparing to send a PROPOSE message to all ACCEPTOR nodes");
        this.proposalNumber++;

        // Dummy implementation for now - should take into consideration progress certificate
        // Generate random value to propose
        if (pc == null) {
            SecureRandom random = new SecureRandom();
            byte[] value = new byte[32];
            random.nextBytes(value);
            proposedValue = value;
        }
        // Resend this message until (p (proposer replicas) + f + 1 ) / 2 <= satisfied.size()
        // Count the number of proposer replicas
        int p = (int) getNodeIds().stream()
                .filter(nodeId -> roles.contains(FabRole.PROPOSER))
                .count();
        // Calculate the threshold for the number of satisfied messages needed
        int threshold = (p + 1) / 2;

        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeout;

        log.info("Sending PROPOSE message to " + p + " nodes...");
        while (System.currentTimeMillis() < endTime && satisfied.size() < threshold) {
            getNodeIds().stream()
                    .filter(nodeId -> roles.contains(FabRole.ACCEPTOR))
                    .forEach(nodeId -> getTransport().
                            sendMessage(getNodeId(),
                                    new ProposeMessage(this.getNodeId(), new Pair(proposalNumber, proposedValue)),
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
        }
    }

    public void starSuspectingLeader(int p) {

    }

    private void proposerOnStart() {
        // If the replica is a proposer, wait for a timeout
        while (pc == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.severe("Thread was interrupted: " + e.getMessage());
            }
        }
    }

    private void learnerOnStart() {
        // If the replica is a learner, wait for a timeout
        while (learned == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.severe("Thread was interrupted: " + e.getMessage());
            }
        }
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
            if (roles.contains(FabRole.ACCEPTOR)) {
                handleProposeMessage(sender, (ProposeMessage) message);
            }
        } else if (message instanceof AcceptMessage) {
            // Learner replicas must process ACCEPT messages
            if (roles.contains(FabRole.LEARNER)) {
                handleAcceptMessage(sender, (AcceptMessage) message);
            }
        } else if (message instanceof SatisfiedMessage) {
            // Proposers must process SATISFIED messages
            if (roles.contains(FabRole.PROPOSER)) {
                handleSatisfiedMessage(sender, (SatisfiedMessage) message);
            }
        } else if (message instanceof LearnMessage) {
            // Learner replicas must process LEARN messages
            if (roles.contains(FabRole.LEARNER)) {
                handleLearnMessage(sender, (LearnMessage) message);
            }
        } else {
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
        if (proposeMessage.getValueAndProposalNumber().getNumber() > proposalNumber) {
            proposalNumber = proposeMessage.getValueAndProposalNumber().getNumber();
            proposedValue = proposeMessage.getValueAndProposalNumber().getValue();
        }
    }

    /**
     * Handle an ACCEPT message sent by an Acceptor replica, received by a Learner replica.
     * @param sender : the nodeId of the sender (an Acceptor replica)
     * @param acceptMessage : the ACCEPT message with the value and proposal number
     */
    private void handleAcceptMessage(String sender, AcceptMessage acceptMessage) {
        // Acceptors must process ACCEPT messages
        // If the ACCEPT message has a higher round number than the current round number, update the round number
        accepted[sender.charAt(0) - 'A'] = acceptMessage.getValueAndProposalNumber();
    }

    private void handleLearnMessage(String sender, LearnMessage learnMessage) {
        learn[sender.charAt(0) - 'A'] = learnMessage.getValueAndProposalNumber();
    }

    /**
     * Handle a SATISFIED message received by a Proposer replica.
     * @param sender : the nodeId of the sender (a Proposer replica)
     * @param satisfiedMessage : the SATISFIED message with the value and proposal number
     */
    private void handleSatisfiedMessage(String sender, SatisfiedMessage satisfiedMessage) {
        satisfied.put(sender, satisfiedMessage.getValueAndProposalNumber());
    }

    public boolean validateProgressCertificate() {
        // Implement progress certificate validation logic here
        return true;
    }

    public void electNewLeader() {
        System.out.println("Electing a new leader...");
        // Implement leader election logic here
    }


    /** Methods for checking the role of the replica. **/
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
