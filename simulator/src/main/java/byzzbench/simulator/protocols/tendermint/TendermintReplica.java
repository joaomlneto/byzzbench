package byzzbench.simulator.protocols.tendermint;

import byzzbench.simulator.LeaderBasedProtocolReplica;
import byzzbench.simulator.protocols.tendermint.MessageLog;
import byzzbench.simulator.protocols.tendermint.message.*;
import byzzbench.simulator.state.TotalOrderCommitLog;
import byzzbench.simulator.transport.MessagePayload;
import byzzbench.simulator.transport.Transport;


import java.io.Serializable;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.extern.java.Log;

@Log
@Getter
public class TendermintReplica extends LeaderBasedProtocolReplica {

    // Blockchain height: the current index of the chain being decided
    private long height;

    // Round: the current round within the height, where multiple rounds might be needed to finalize a block
    private long round;

    // Step: the current step within the round (PROPOSE, PREVOTE, PRECOMMIT)
    private Step step;

    //
    private List<String> decision;

    // Hash of the block this replica is "locked" on (used to ensure no conflicting decisions are made)
    private byte[] lockedValue;

    // Round number of the block this replica is locked on
    private long lockedRound;

    // Hash of the block this replica has validated
    private String validValue;

    // Round number of the block this replica has validated
    private long validRound;

    // Assigned powers of each replica in the network
    private final Map<String, Integer> votingPower = new HashMap<>();

    public TendermintReplica(String nodeId, SortedSet<String> nodeIds, Transport transport) {
        // Initialize replica with node ID, a list of other nodes, transport, and a commit log
        super(nodeId, nodeIds, transport, new TotalOrderCommitLog());
        this.height = 0;
        this.round = 0;
        this.step = Step.PROPOSE;
        this.decision = new ArrayList<>();
        this.lockedValue = null;
        this.lockedRound = -1;
        this.validValue = null;
        this.validRound = -1;
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
        // Validate if the message is signed by the correct proposer
        if (!proposalMessage.isSignedBy(proposer(height, round))) {
            log.warning("Message not from proposer for this round: " + proposalMessage);
            return;
        }

        // Compute the digest for the received proposal
        byte[] computedDigest = this.digest(proposalMessage.getDigest());

        // Validate the digest
        if (!Arrays.equals(computedDigest, proposalMessage.getDigest())) {
            log.warning("Invalid digest for the proposal: " + proposalMessage);
            return;
        }

        // Process the proposal
        log.info("Valid proposal received: " + proposalMessage);

        if (isInitialProposal(proposalMessage)) {
            log.info("Initial proposal accepted: " + proposalMessage);
            broadcastPrevote(height, round, getNodeId());
        } else if (isSubsequentProposal(proposalMessage)) {
            log.info("Subsequent proposal received: " + proposalMessage);

            // Verify if the proposal is valid and if it's in the right step to be accepted
            if (valid(proposalMessage.getDigest()) &&
                    (this.step.equals(Step.PREVOTE) || this.step.equals(Step.PRECOMMIT))) {

                // Lock the value if currently in the PREVOTE step
                if (this.step == Step.PREVOTE) {
                    this.lockedValue = proposalMessage.getDigest();
                    this.lockedRound = proposalMessage.getRound();
                    broadcastPrecommit(height, round, getNodeId());
                    this.step = Step.PRECOMMIT;
                }

                this.validValue = proposalMessage.getValue();
                this.validRound = proposalMessage.getRound();
            } else if (this.decision.get((int) height) == null) {
                // Finalize decision if valid
                if (valid(proposalMessage.getValue())) {
                    this.decision.set((int) height, proposalMessage.getValue());
                    this.height++;
                    reset();
                }
            } else if (getStep().equals(Step.PROPOSE)
                    && proposalMessage.getValidRound() >= 0
                    && proposalMessage.getValidRound() < getRound()) {

                // Validate subsequent proposals and possibly broadcast prevote
                if (valid(proposalMessage.getValue()) &&
                        (lockedRound <= proposalMessage.getValidRound() ||
                                lockedValue.equals(proposalMessage.getValue()))) {
                    broadcastPrevote(height, round, proposalMessage.getSignedBy());
                } else {
                    broadcastPrevote(height, round, null);
                }
                step = Step.PREVOTE;
            }
            broadcastPrevote(height, round, null);
        } else {
            log.warning("Invalid proposal received: " + proposalMessage);
            broadcastPrevote(height, round, null);
        }

        // Advance to the next step
        step = Step.PREVOTE;
    }


    private void reset() {
        this.lockedValue = null;
        this.lockedRound = -1;
        this.validValue = null;
        this.validRound = -1;
    }

    // This variant of the Byzantine consensus problem has an application-specific valid() predicate to indicate
    // whether a value is valid. In the context of blockchain systems, for example, a value is not valid if it does not
    // contain an appropriate hash of the last value (block) added to the blockchain.
    private boolean valid(byte[] digest) {
        return true;
    }

    /**
     * 28: upon ⟨PROPOSAL, hp, roundp, v, vr⟩ from proposer(hp, roundp) AND 2f + 1 ⟨PREVOTE, hp, vr, id(v)⟩
     * @param proposalMessage
     * @return true if the proposal is a subsequent proposal
     */
    private boolean isSubsequentProposal(ProposalMessage proposalMessage) {
        if (proposalMessage.getValidRound() != -1) {
            // TODO: 2f + 1 ⟨PREVOTE, hp, vr, id(v)⟩ messages received
            // for now just true
            return true;
        }
        return false;
    }

    /**
     * 22: upon ⟨PROPOSAL, hp, roundp, v, −1⟩ from proposer(hp, roundp) while stepp = propose do
     * 23: if valid(v) ∧ (lockedRoundp = −1 ∨ lockedV aluep = v) then
     * 24: broadcast ⟨PREVOTE, hp, roundp, id(v)⟩
     * 25: else
     * 26: broadcast ⟨PREVOTE, hp, roundp, nil⟩
     * 27: stepp ← prevote
     * @param proposalMessage
     * @return true if the proposal is the initial proposal
     */
    private boolean isInitialProposal(ProposalMessage proposalMessage) {
        if (proposalMessage.getValidRound() == -1) {
            if (proposer(height, round).equals(proposalMessage.getSignedBy()) && step == Step.PROPOSE) {
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * Broadcasts the block proposal to all validators in the network.
     * Intended Behavior:
     * - Send a proposal message containing the block details to all peers.
     */
    protected void broadcastProposal(long height, long round, int proposal, int validRound) {
        log.info("Broadcasting proposal as leader: " + getNodeId());
        ProposalMessage proposalMessage = new ProposalMessage(getNodeId(), height, round, proposal, validRound);
        broadcastMessage(proposalMessage);
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
                lockedValue = prevoteMessage.getBlockHash();
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
    private void broadcastPrevote(long height, long round, String nodeId) {
        PrevoteMessage prevoteMessage = new PrevoteMessage(height, round, nodeId);
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
        if (hasQuorumForBlock(lockedValue)) {
            blockHashToPrecommit = lockedValue;
        } else if (hasQuorumForBlock(null)) {
            lockedValue = null; // Unlock if quorum exists for nil
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
        if (!commitMessage.getBlockHash().equals(lockedValue)) {
            log.warning("Commit message does not match locked block hash.");
        } else {
            log.info("Block already committed locally: " + lockedValue);
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
     * <p>
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
     * <p>
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
     * <p>
     * This includes checks for message type, blockchain height, and round number.
     *
     * @param message The message to validate.
     * @return True if valid, false otherwise.
     */
    protected boolean validateMessage(MessagePayload message) {
        // Check if the message type matches a known type
        if (!(message instanceof ProposalMessage || message instanceof PrevoteMessage || message instanceof PrecommitMessage || message instanceof CommitMessage)) {
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
     * <p>
     * Routes the message to the appropriate handler based on its type.
     *
     * @param sender  The ID of the sender.
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
     * @param request  The request payload.
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
        startRound(0);
    }

    /**
     * Starts a new round of the consensus protocol.
     *
     * @param roundNumber The round number to start.
     */
    private void startRound(long roundNumber) {
        setRound(roundNumber);
        step = Step.PROPOSE;
        int proposal = -1;
        if (proposer(height, roundNumber).equals(getNodeId())) {
            if (validValue != null) {
                proposal = Integer.parseInt(validValue);
            } else {
                // TODO: implement getValue()
                proposal = getValue();
            }
            broadcastProposal(height, round, proposal, validRound);
        }

    }

    private void setRound(long roundNumber) {
        String leaderId = proposer(height, roundNumber);
        // view is the same as round in Tendermint
        this.round = roundNumber;
    }

    /**
     * For now just returns A
     * // We assume that the proposer selection function is weighted round-robin, where processes are
     * // rotated proportional to their voting power. A validator with more voting power is selected more frequently,
     * // proportional to its power. More precisely, during a sequence of rounds of size n, every process is proposer
     * // in a number of rounds equal to its voting power.
     */
    private String proposer(long height, long round) {
        return "A";
    }
}