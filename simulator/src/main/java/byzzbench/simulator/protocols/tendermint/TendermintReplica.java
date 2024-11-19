package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.protocols.tendermint.message.*;
import byzzbench.simulator.state.CommitLog;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;

import java.io.Serializable;
import java.util.*;
import lombok.Getter;
import lombok.extern.java.Log;

@Log
@Getter
public class TendermintReplica extends LeaderBasedProtocolReplica {

    // Blockchain height: the current index of the chain being decided
    private long height = 0;

    // Round: the current round within the height, where multiple rounds might be needed to finalize a block
    private long round = 0;

    // Hash of the block this replica is "locked" on (used to ensure no conflicting decisions are made)
    private String lockedBlockHash = null;

    // Proposed block hash for the current round
    private String proposedBlockHash = null;

    // Set of received votes during the current round
    private final Set<GenericVoteMessage> receivedVotes = new HashSet<>();

    //Map of voting power for each node
    private final Map<String, Integer> votingPower = new HashMap<>();

    public TendermintReplica(String nodeId, SortedSet<String> nodeIds, Transport transport) {
        // Initialize replica with node ID, a list of other nodes, transport, and a commit log
        super(nodeId, nodeIds, transport, new TotalOrderCommitLog());
        this.initialize();
    }

    // ============================
    // Propose Step
    // ============================

    /**
     * Handles the proposal from the designated leader for the current round.
     * Intended Behavior:
     * - Validate the proposal's height and round.
     * - Accept the proposal if valid, otherwise reject it.
     */
    protected void handleProposal(ProposalMessage proposalMessage) {
        if (validateMessage(proposalMessage)) {
            // Accept the proposed block and store its hash
            proposedBlockHash = proposalMessage.getBlockHash();
            log.info("Proposal received and accepted: " + proposalMessage);
        } else {
            log.warning("Invalid proposal received: " + proposalMessage);
        }
    }


    /**
     * Creates a block proposal for this replica.
     * Intended Behavior:
     * - If locked on a block, propose that block.
     * - Otherwise, create a new block to propose.
     */
    protected ProposalMessage createProposal() {
        String blockHash = (lockedBlockHash != null) ? lockedBlockHash : createBlock().getType();
        return new ProposalMessage(getNodeId(), height, round, blockHash);
    }

    /**
     * Broadcasts the block proposal to all validators in the network.
     * Intended Behavior:
     * - Send a proposal message containing the block details to all peers.
     */
    protected void broadcastProposal() {
        // Check if this replica is the current leader
        if (!getNodeId().equals(getLeaderId())) {
            log.info("Not the leader for this round. Skipping proposal broadcast.");
            return;
        }

        log.info("Broadcasting proposal as leader: " + getNodeId());
        ProposalMessage proposalMessage = createProposal();
        broadcastMessage(proposalMessage);
    }

    private Block createBlock() {
        // Intended Behavior:
        // - Generate a new block with transactions and metadata for the current height.
        return new Block(height, new TreeSet<>(), proposedBlockHash);
    }


    // ============================
    // Prevote Step
    // ============================

    /**
     * Handles a prevote message from another validator.
     * Intended Behavior:
     * - Validate the prevote and track votes for the block hash (or nil).
     * - If 2/3 of validators prevote for a block, lock onto it.
     */
    protected void handlePrevote(PrevoteMessage prevoteMessage) {
        if (!validateMessage(prevoteMessage)) {
            log.warning("Invalid prevote received: " + prevoteMessage);
            return;
        }

        log.info("Prevote received: " + prevoteMessage);
        receivedVotes.add(prevoteMessage);

        if (hasQuorumForBlock(prevoteMessage.getBlockHash())) {
            log.info("Quorum reached for block hash: " + prevoteMessage.getBlockHash());
            if (prevoteMessage.getBlockHash() != null) {
                lockedBlockHash = prevoteMessage.getBlockHash();
            }
        }
    }

    /**
     * Checks if 2/3 quorum is reached for a block hash.
     * Intended Behavior:
     * - Count votes for the specified block hash.
     * - Return true if they represent at least 2/3 of the total voting power.
     */
    private boolean hasQuorumForBlock(String blockHash) {
        int totalPower = 0;
        int quorumPower = 0;

        for (GenericVoteMessage vote : receivedVotes) {
            // Match votes for the given block hash
            if (Objects.equals(vote.getBlockHash(), blockHash)) {
                String voter = vote.getAuthor();
                quorumPower += votingPower.getOrDefault(voter, 0);
            }
            totalPower += votingPower.getOrDefault(vote.getAuthor(), 0);
        }

        // Check if quorumPower reaches 2/3 of totalPower
        return quorumPower >= (2 * totalPower) / 3;
    }


    /**
     * Creates a prevote message for the specified block hash.
     *
     * @param blockHash The hash of the block being voted on (null for NIL).
     * @return The created PrevoteMessage.
     */
    protected PrevoteMessage createPrevote(String blockHash) {
        return new PrevoteMessage(getNodeId(), height, round, blockHash);
    }

    /**
     * Broadcasts a prevote message for the current round.
     * Intended Behavior:
     * - Vote for the locked block, proposed block, or nil, depending on the state.
     */
    private void broadcastPrevote() {
        String blockHashToPrevote = (lockedBlockHash != null) ? lockedBlockHash
                : (proposedBlockHash != null) ? proposedBlockHash
                : null;

        PrevoteMessage prevoteMessage = createPrevote(blockHashToPrevote);
        broadcastMessage(prevoteMessage);
    }

    // ============================
    // Precommit Step
    // ============================

    /**
     * Handles a precommit message during the precommit step.
     * Intended Behavior:
     * - Validate and track precommits for the block.
     * - If 2/3 of validators precommit, commit the block.
     */
    protected void handlePrecommit(PrecommitMessage precommitMessage) {
        if (!validateMessage(precommitMessage)) {
            log.warning("Invalid precommit received: " + precommitMessage);
            return;
        }

        log.info("Precommit received: " + precommitMessage);
        receivedVotes.add(precommitMessage);

        if (hasQuorumForBlock(precommitMessage.getBlockHash())) {
            log.info("Quorum reached for precommit: " + precommitMessage.getBlockHash());
            if (precommitMessage.getBlockHash() != null) {
                handleCommit(precommitMessage.getBlockHash());
            }
        }
    }

    /**
     * Creates and broadcasts a precommit message for the current round and block.
     * If a quorum of 2/3 prevotes exists for a block, precommit that block.
     * If a quorum of 2/3 prevotes exists for nil, unlock and precommit nil.
     */
    private void broadcastPrecommit() {
        String blockHashToPrecommit = null;

        // Check for quorum of prevotes
        if (hasQuorumForBlock(lockedBlockHash)) {
            blockHashToPrecommit = lockedBlockHash;
        } else if (hasQuorumForBlock(null)) {
            lockedBlockHash = null; // Unlock if quorum exists for nil
        }

        PrecommitMessage precommitMessage = createPrecommit(blockHashToPrecommit);
        broadcastMessage(precommitMessage);
    }

    /**
     * Creates a precommit message for the specified block hash.
     *
     * @param blockHash The hash of the block being precommitted (or null for nil).
     * @return The created PrecommitMessage.
     */
    protected PrecommitMessage createPrecommit(String blockHash) {
        return new PrecommitMessage(getNodeId(), height, round, blockHash);
    }


    // ============================
    // Commit Step
    // ============================

    /**
     * Handles the commit process for a block.
     * Intended Behavior:
     * - Finalize the block and add it to the blockchain.
     * - Broadcast a commit message and proceed to the next height.
     */
    protected void handleCommit(String blockHash) {
        log.info("Committing block with hash: " + blockHash);

        Block block = new Block(height, new TreeSet<>(), blockHash);
        commitBlock(block);
        broadcastCommit(blockHash);
        advanceToNextHeight();
    }

    /**
     * Handles a received commit message from another validator.
     *
     * @param commitMessage The commit message received from a peer.
     */
    protected void handleCommitMessage(CommitMessage commitMessage) {
        if (!validateMessage(commitMessage)) {
            log.warning("Invalid commit message received: " + commitMessage);
            return;
        }

        log.info("Commit message received: " + commitMessage);

        // Ensure consistency of the local blockchain
        if (!commitMessage.getBlockHash().equals(lockedBlockHash)) {
            log.warning("Commit message does not match locked block hash.");
        } else {
            log.info("Block already committed locally: " + lockedBlockHash);
        }
    }

    /**
     * Commits the block to the local blockchain and notifies observers.
     *
     * @param block The block to commit.
     */
    protected void commitBlock(Block block) {
        log.info("Adding block to the local blockchain: " + block);
        commitOperation(block); // Adds block to commit log

        // Notify observers (if any) of the new commit
        notifyObserversLocalCommit(block);
    }

    /**
     * Broadcasts a commit message for the specified block hash.
     *
     * @param blockHash The hash of the block being committed.
     */
    private void broadcastCommit(String blockHash) {
        CommitMessage commitMessage = new CommitMessage(getNodeId(), height, blockHash);
        broadcastMessage(commitMessage);
    }



    // ============================
    // Timeouts and Round Management
    // ============================

    /**
     * Handles a timeout for the current round.
     *
     * This method is triggered when the round's timeout expires without reaching consensus.
     *
     * @param round The round that timed out.
     */
    protected void handleRoundTimeout(long round) {
        if (this.round != round) {
            log.warning("Timeout received for a past round: " + round);
            return;
        }

        log.info("Round timeout occurred for round: " + round);

        // Advance to the next round
        advanceRound(round + 1);
    }


    /**
     * Advances the round and resets necessary state.
     *
     * This is called when the current round cannot achieve consensus, and the protocol proceeds to the next round.
     *
     * @param newRound The new round to advance to.
     */
    protected void advanceRound(long newRound) {
        log.info("Advancing to round: " + newRound);
        this.round = newRound;

        // Clear state specific to the previous round
        proposedBlockHash = null;
        receivedVotes.clear();

        // Reset round-specific state and start new round's propose step
        broadcastProposal();
    }


    // ============================
    // Miscellaneous
    // ============================

    /**
     * Validates an incoming message for correctness.
     *
     * This includes checks for message type, blockchain height, and round number.
     *
     * @param message The message to validate.
     * @return True if valid, false otherwise.
     */
    protected boolean validateMessage(MessagePayload message) {
        // Check if the message type matches a known type
        if (!(message instanceof ProposalMessage ||
                message instanceof PrevoteMessage ||
                message instanceof PrecommitMessage ||
                message instanceof CommitMessage)) {
            log.warning("Unknown message type: " + message.getType());
            return false;
        }

        // Check that the message height matches the current height
        if (message instanceof GenericVoteMessage voteMessage) {
            if (voteMessage.getHeight() != height) {
                log.warning("Message height mismatch: " + voteMessage.getHeight());
                return false;
            }
        }

        return true;
    }


    /**
     * Handles receipt of a generic Tendermint message.
     *
     * @param sender The ID of the sender.
     * @param message The received message.
     * @throws Exception If an error occurs while handling the message.
     */
    /**
     * Handles receipt of a generic Tendermint message.
     *
     * Routes the message to the appropriate handler based on its type.
     *
     * @param sender The ID of the sender.
     * @param message The received message.
     * @throws Exception If an error occurs while handling the message.
     */
    @Override
    public void handleMessage(String sender, MessagePayload message) throws Exception {
        if (!validateMessage(message)) {
            log.warning("Invalid message received from " + sender);
            return;
        }

        if (message instanceof ProposalMessage) {
            handleProposal((ProposalMessage) message);
        } else if (message instanceof PrevoteMessage) {
            handlePrevote((PrevoteMessage) message);
        } else if (message instanceof PrecommitMessage) {
            handlePrecommit((PrecommitMessage) message);
        } else if (message instanceof CommitMessage) {
            handleCommitMessage((CommitMessage) message);
        } else {
            log.warning("Unhandled message type: " + message.getType());
        }
    }


    /**
     * Handles a client request.
     *
     * @param clientId The ID of the client.
     * @param request The request payload.
     * @throws Exception If an error occurs while handling the request.
     */
    @Override
    public void handleClientRequest(String clientId, Serializable request) throws Exception {
        throw new UnsupportedOperationException("Client requests not supported in FastHotStuff");
    }

    /**
     * Initializes the replica state and starts the consensus process.
     */
    @Override
    public void initialize() {
        log.info("Initializing Tendermint replica: " + getNodeId());
        initializeVotingPower(); // Initialize voting power
        this.setView(1);
    }

    // Initialize the voting power map
    private void initializeVotingPower() {
        votingPower.put("A", 50); // Assign 50% voting power to replica A
        votingPower.put("B", 20); // Assign 20% voting power to replica B
        votingPower.put("C", 20); // Assign 20% voting power to replica C
        votingPower.put("D", 10); // Assign 10% voting power to replica D
    }

    private void setView(long viewNumber) {
        String leaderId = (viewNumber == 1) ? "A" : computePrimaryId(viewNumber, this.votingPower);
        this.setView(viewNumber, leaderId);
    }

    private String computePrimaryId(long viewNumber, Map<String, Integer> votingPower) {
        // Sort replicas by ID for consistency
        List<String> sortedReplicas = new ArrayList<>(votingPower.keySet());
        sortedReplicas.sort(String::compareTo);

        // Calculate total voting power
        int totalVotingPower = votingPower.values().stream().mapToInt(Integer::intValue).sum();

        // Calculate the cumulative weight for each replica
        List<Integer> cumulativeWeights = new ArrayList<>();
        int cumulativeSum = 0;
        for (String replica : sortedReplicas) {
            cumulativeSum += votingPower.getOrDefault(replica, 0);
            cumulativeWeights.add(cumulativeSum);
        }

        // Determine the leader based on the view number
        int modValue = (int) (viewNumber % totalVotingPower);
        for (int i = 0; i < cumulativeWeights.size(); i++) {
            if (modValue < cumulativeWeights.get(i)) {
                return sortedReplicas.get(i);
            }
        }

        // Default to the first replica if no match (should never happen)
        return sortedReplicas.get(0);
    }

    /**
     * Advances the blockchain to the next height and resets state for the new round.
     */
    private void advanceToNextHeight() {
        log.info("Advancing to next height: " + (height + 1));
        height += 1;
        round = 0;
        lockedBlockHash = null;
        proposedBlockHash = null;
        receivedVotes.clear();

        // TODO: Reinitialize for the new height if necessary
        initialize();
    }

}
