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

    @Getter
    private final long timeout;
    @Getter
    private final long viewNumber;

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
        this.viewNumber = 0;
    }

    @Override
    public void initialize() {
        System.out.println("Initializing FAB replica " + getNodeId());
        // Initialize the replica based on the roles it has
        if (roles.contains(FabRole.ACCEPTOR)) {
            this.acceptedValue = null;
        }

        if (roles.contains(FabRole.LEARNER)) {
            this.accepted = new Pair[getNodeIds().size()];
            this.learn = new Pair[getNodeIds().size()];
            this.learned = null;
        }

        if (roles.contains(FabRole.PROPOSER)) {
            this.proposalNumber = 0;
            this.proposedValue = null;
            this.pc = null;
        }
    }

    public void onStart() {
        // If the replica is a leader, send a PROPOSE message to all ACCEPTOR nodes
        if (isLeader()) {
            // Send a PROPOSE message to all ACCEPTOR nodes
            this.proposalNumber++;
            this.proposedValue = new byte[0];
            // Resend this message until (p (proposer replicas) + f + 1 ) / 2 <= satisfied.size()
            // Count the number of proposer replicas
            int p = 0;
            for (String nodeId : getNodeIds()) {
                if (roles.contains(FabRole.PROPOSER)) {
                    p++;
                }
            }

            while ((p + 1) / 2 > satisfied.size()) {
                for (String nodeId : getNodeIds()) {
                    if (roles.contains(FabRole.ACCEPTOR)) {
                        getTransport().sendMessage(getNodeId(), new ProposeMessage(this.getNodeId(), proposalNumber, proposedValue), nodeId);
                    }
                }
            }
        }

        // If the replica is a proposer, wait for a timeout
        if (roles.contains(FabRole.PROPOSER)) {
            while (pc == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // If the replica is an acceptor, do nothing in the onStart method

        // If the replica is a learner, wait for a timeout
        if (roles.contains(FabRole.LEARNER)) {
            while (learned == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void handleClientRequest(String clientId, Serializable request) throws Exception {
        // If the replica is a proposer, send a PROPOSE message to all ACCEPTOR nodes
        if (roles.contains(FabRole.PROPOSER) && isLeader()) {
            for (String nodeId : getNodeIds()) {
                getTransport().sendMessage(getNodeId(), new ProposeMessage(this.getNodeId(), proposalNumber, (byte[]) request), nodeId);
            }
        }
    }

    @Override
    public void handleMessage(String sender, MessagePayload message) throws Exception {
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
        if (proposeMessage.getRound() > proposalNumber) {
            proposalNumber = proposeMessage.getRound();
            proposedValue = proposeMessage.getValue();
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
}
