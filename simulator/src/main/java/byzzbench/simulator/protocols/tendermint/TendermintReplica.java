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
    private long height = 0; // Current blockchain height
    private long round = 0;  // Current round
    private String lockedBlockHash = null; // Block hash this replica is locked on
    private String proposedBlockHash = null; // Block hash proposed in the current round
    private final Set<GenericVoteMessage> receivedVotes = new HashSet<>(); // Received votes for the current round

    public TendermintReplica(String nodeId, SortedSet<String> nodeIds, Transport transport) {
        super(nodeId, nodeIds, transport, new TotalOrderCommitLog());
    }

    // ============================
    // Propose Step
    // ============================

    /**
     * Handles the propose step where the designated leader proposes a block.
     *
     * @param proposalMessage The proposal message containing block details.
     */
    protected void handleProposal(ProposalMessage proposalMessage) {
        // Validate proposal (e.g., check block hash, height, and round).
        if (validateMessage(proposalMessage)) {
            proposedBlockHash = proposalMessage.getBlockHash();
            log.info("Proposal received and accepted: " + proposalMessage);
        } else {
            log.warning("Invalid proposal received: " + proposalMessage);
        }
    }


    /**
     * Creates a proposal message for the current block.
     *
     * @return The created ProposalMessage.
     */
    protected ProposalMessage createProposal() {
        /// if lockedBlockHash is not null, propose the locked block
        /// else propose a new block
        String blockHash = (lockedBlockHash != null) ? lockedBlockHash : createBlock().getType();
        return new ProposalMessage(getNodeId(), height, round, blockHash);
    }

    private void broadcastProposal() {
        ProposalMessage proposalMessage = createProposal();
        broadcastMessage(proposalMessage);
    }

    private Block createBlock() {
        // TODO: Generate a new block with transactions and metadata.
        return new Block(height, new TreeSet<>(), proposedBlockHash);
    }


    // ============================
    // Prevote Step
    // ============================

    /**
     * Handles a received prevote message during the prevote step.
     *
     * @param prevoteMessage The prevote message received from another validator.
     */
    protected void handlePrevote(PrevoteMessage prevoteMessage) {
        if (!validateMessage(prevoteMessage)) {
            log.warning("Invalid prevote received: " + prevoteMessage);
            return;
        }

        log.info("Prevote received: " + prevoteMessage);

        // Track the vote for the given block hash or NIL
        receivedVotes.add(prevoteMessage);

        // Check if a quorum of 2/3 prevotes for a specific block or nil is reached
        if (hasQuorumForBlock(prevoteMessage.getBlockHash())) {
            log.info("Quorum reached for block hash: " + prevoteMessage.getBlockHash());
            if (prevoteMessage.getBlockHash() != null) {
                lockedBlockHash = prevoteMessage.getBlockHash(); // Lock onto this block
            }
        }
    }

    /**
     * Checks if there is a quorum of 2/3 prevotes for the given block hash.
     *
     * @param blockHash The block hash to check (null for NIL).
     * @return True if a quorum is reached, false otherwise.
     */
    private boolean hasQuorumForBlock(String blockHash) {
        long totalVotes = receivedVotes.stream()
                .filter(vote -> Objects.equals(vote.getBlockHash(), blockHash))
                .count();

        return totalVotes >= (2.0 / 3.0) * getNodeIds().size();
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
     * Creates and broadcasts a prevote message for the current round and block.
     *
     * If the validator is locked on a block, it prevotes for that block.
     * If there is no lock but a valid proposal exists, it prevotes for the proposal.
     * Otherwise, it prevotes nil.
     */
    private void broadcastPrevote() {
        String blockHashToPrevote = (lockedBlockHash != null) ? lockedBlockHash
                : (proposedBlockHash != null) ? proposedBlockHash
                : null; // NIL vote

        PrevoteMessage prevoteMessage = createPrevote(blockHashToPrevote);
        broadcastMessage(prevoteMessage);
    }

    // ============================
    // Precommit Step
    // ============================

    /**
     * Handles a received precommit message during the precommit step.
     *
     * @param precommitMessage The precommit message received from another validator.
     */
    protected void handlePrecommit(PrecommitMessage precommitMessage) {
        if (!validateMessage(precommitMessage)) {
            log.warning("Invalid precommit received: " + precommitMessage);
            return;
        }

        log.info("Precommit received: " + precommitMessage);

        // Track the vote for the given block hash or nil
        receivedVotes.add(precommitMessage);

        // Check if a quorum of 2/3 precommits for a specific block or nil is reached
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
     *
     * This method is triggered when a quorum of precommits is reached for a block.
     *
     * @param blockHash The hash of the block to commit.
     */
    protected void handleCommit(String blockHash) {
        log.info("Committing block with hash: " + blockHash);

        // Create and finalize the block (assuming transactions are stored elsewhere)
        Block block = new Block(height, new TreeSet<>(), blockHash);

        // Commit the block locally
        commitBlock(block);

        // Broadcast the commit message to other validators
        broadcastCommit(blockHash);

        // Transition to the next height
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
        this.height = 0;
        this.round = 0;
        this.lockedBlockHash = null;
        this.proposedBlockHash = null;
        this.receivedVotes.clear();

        // Start the first propose step
        broadcastProposal();
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
